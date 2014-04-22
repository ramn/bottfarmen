package se.ramn.bottfarmen.simulation

import collection.JavaConverters._
import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq
import util.Random

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.Action
import se.ramn.bottfarmen.simulation.entity.Move
import se.ramn.bottfarmen.simulation.entity.Attack
import se.ramn.bottfarmen.simulation.view.SimulationView
import se.ramn.bottfarmen.simulation.view.SimulationViewImpl
import se.ramn.bottfarmen.util.Logging


class SimulationImpl(
  val commanders: Set[BotCommander],
  scenario: Scenario
) extends Simulation with Logging {
  lazy val view: SimulationView = new SimulationViewImpl(commanders)
  lazy val gameStateApiGateway = new GameStateApiGateway(commanders, scenario)
  val foodSpawner = new FoodSpawner(scenario)

  var turnNo = 0
  var isGameOver = false
  var victor: Option[BotCommander] = None

  override def botCommanders = view.botCommanders

  override def bots = view.bots

  override def spawnedFood = foodSpawner.spawnedFood

  override def doTurn: Unit = {
    if (!isGameOver) {
      turnNo += 1

      botsGrabFood()
      foodSpawner.update(scenario.tilemap, turnNo)

      val actionsForTurn = actions(extractCommands())
      resolveAttackActions(actionsForTurn)
      resolveMoveActions(actionsForTurn)
      checkForVictory()
    } else {
      println("Game is already over, no more turn will be processed")
    }
  }

  def botsGrabFood() = {
    def allLivingBots = commanders.flatMap(_.bots).filter(_.isAlive)
    val botsStandingOnFood =
      allLivingBots.filter { bot => foodSpawner.positionHasFood(bot.position) }
    botsStandingOnFood foreach { foodGrabbingBot =>
      val commander = foodGrabbingBot.commander
      val homeBasePos = commander.homeBase.position
      val homeBaseOccupied = allLivingBots.exists(_.position == homeBasePos)
      if (!homeBaseOccupied &&
        commander.bots.size < scenario.maxBotCountPerCommander) {
        foodSpawner.consumeFood(foodGrabbingBot.position)
        commander.spawnBot(homeBasePos)
      }
    }
  }

  def resolveAttackActions(actions: Seq[Action]) = {
    actions foreach {
      case Attack(attacker, targetPos) =>
        val targetIsWithinRange =
          (attacker.position.neighbours + attacker.position)(targetPos)
        if (targetIsWithinRange) {
          val occupantsAtTargetPos =
            commanders.flatMap(_.bots)
              .filter(_.position == targetPos)
              .filterNot(_ == attacker)
          occupantsAtTargetPos foreach { victim =>
            victim.takeDamage(attacker.attackStrength)
          }
          val enemyBaseAtTargetOpt: Option[Base] = commanders
              .filterNot(_ == attacker.commander)
              .map(_.homeBase)
              .filter(_.position == targetPos)
              .headOption
          // Only hit enemy base if no defender stands on it
          if (occupantsAtTargetPos.isEmpty) {
            enemyBaseAtTargetOpt foreach { enemyBase =>
              enemyBase.takeDamage(attacker.attackStrength)
            }
          }
        }
      case _ =>
    }
  }

  def resolveMoveActions(actions: Seq[Action]) = {
    val moveActions: Seq[Move] = actions.collect { case action: Move => action }
    val livingBots = commanders.flatMap(_.bots).filter(_.hitpoints > 0).toSet
    val movers = moveActions
      .filter(move => livingBots(move.bot))
      .map(move => move.bot -> move.position)
      .toMap
    val stillBots = livingBots -- movers.keySet
    val moveResolver = new MoveResolver(movers, stillBots, scenario)
    val unhandledMovers = moveResolver.resolve()
    unhandledMovers foreach { unhandledMover =>
      val (bot, pos) = unhandledMover
      println(s"Could not move bot $bot to $pos")
    }
  }

  def checkForVictory() = {
    val withBaseStillStanding = commanders.filter(_.homeBase.isAlive)
    val withBotsAlive = commanders.filter(_.bots.filter(_.isAlive).size > 0)
    val allBasesAreRazed = withBaseStillStanding.size == 0
    val hasLastBaseStanding = withBaseStillStanding.size == 1
    val hasLastBotStanding = withBotsAlive.size == 1
    if (hasLastBaseStanding || allBasesAreRazed || hasLastBotStanding) {
      isGameOver = true
      if (hasLastBaseStanding) {
        victor = withBaseStillStanding.headOption
      } else if (hasLastBotStanding) {
        victor = withBotsAlive.headOption
      }
    }
  }

  def actions(
    commandsByCommander: Map[BotCommander, Seq[api.Command]]
  ): Seq[Action] = {
    val commanderCommandPairs: Seq[(BotCommander, api.Command)] = for {
      (commander, commands) <- commandsByCommander.toList
      command <- commands
    } yield (commander, command)
    val actions: Seq[Action] =
      commanderCommandPairs flatMap { case (commander, command) =>
        val pipe = validateAttack(commander) orElse validateMove(commander)
        pipe(command)
      }
    filterMaxOneCommandPerBot(actions)
  }

  def filterMaxOneCommandPerBot(actions: Seq[Action]): Seq[Action] = {
    val botsWithSingleAction =
      actions.foldLeft(Map.empty[Bot, Action]) { (memo, action) =>
        if (memo contains action.bot) {
          memo
        } else {
          memo.updated(action.bot, action)
        }
      }
    // return actions with sort order preserved
    actions.filter(botsWithSingleAction.values.toSet)
  }

  def extractCommands(): Map[BotCommander, Seq[api.Command]] =
    commanders.map { commander =>
      val commands = commander.requestCommands(gameStateFor(commander))
      (commander -> commands)
    }.toMap

  def gameStateFor(commander: BotCommander): api.GameState =
    gameStateApiGateway.forCommander(
      commander,
      turnNo,
      foodSpawner.spawnedFood)

  def validateMove(
    commander: BotCommander
  ): PartialFunction[api.Command, Option[Action]] = {
    case api.Move(botId, step) if "nsew".toSet.contains(step) =>
      val botMaybe = commander.bots
        .filter(_.isAlive)
        .find(_.id == botId)
      val maybeAction = botMaybe flatMap { bot =>
        val (targetRow, targetCol) = step match {
          case 'n' => (bot.row - 1) -> bot.col
          case 's' => (bot.row + 1) -> bot.col
          case 'w' => bot.row -> (bot.col - 1)
          case 'e' => bot.row -> (bot.col + 1)
        }
        val targetPos = Position(row=targetRow, col=targetCol)
        val isWithinMap = scenario.tilemap.isWithinMap(targetPos)
        val targetTileOpt = scenario.tilemap.tile(targetPos)
        val mountain = '^'
        val isWalkable = targetTileOpt.map(_ != mountain).getOrElse(false)
        if (isWithinMap && isWalkable) {
          Some(Move(bot, targetPos))
        } else {
          None
        }
      }
      maybeAction
  }

  def validateAttack(
    commander: BotCommander
  ): PartialFunction[api.Command, Option[Action]] = {
    case api.Attack(botId, targetRow, targetCol) =>
      val botMaybe = commander.bots
        .filter(_.isAlive)
        .find(_.id == botId)
      val targetPos = Position(row=targetRow, col=targetCol)
      val isWithinMap = scenario.tilemap.isWithinMap(targetPos)
      for {
        bot <- botMaybe
        if isWithinMap
      } yield Attack(bot, targetPos)
  }
}
