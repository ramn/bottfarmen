package se.ramn.bottfarmen.simulation

import collection.JavaConverters._
import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.Action
import se.ramn.bottfarmen.simulation.entity.Move
import se.ramn.bottfarmen.simulation.entity.Attack
import se.ramn.bottfarmen.simulation.view.SimulationView


class SimulationImpl(
  val commanders: Set[BotCommander],
  scenario: Scenario
) extends Simulation {
  lazy val view = new SimulationView(commanders)
  lazy val gameStateApiGateway = new GameStateApiGateway(commanders, scenario)

  var turnNo = 0

  override def botCommanders = view.botCommanders

  override def bots = view.bots

  override def doTurn: Unit = {
    turnNo += 1

    val actionsForTurn = actions(extractCommands())
    resolveAttackActions(actionsForTurn)
    resolveMoveActions(actionsForTurn)
  }

  def resolveAttackActions(actions: Seq[Action]) = {
    actions foreach {
      case Attack(attacker, targetPos) =>
        val occupantsAtTargetPos =
          commanders.flatMap(_.bots).filter(_.position == targetPos)
        occupantsAtTargetPos foreach { victim =>
          victim.takeDamage(attacker.attackStrength)
        }
        val enemyBaseAtTargetOpt: Option[Base] = commanders
            .filterNot(_ == attacker.commander)
            .map(_.homeBase)
            .filter(_.position == targetPos)
            .headOption
        enemyBaseAtTargetOpt foreach { enemyBase =>
          enemyBase.takeDamage(attacker.attackStrength)
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
    gameStateApiGateway.forCommander(commander, turnNo)

  def validateMove(
    commander: BotCommander
  ): PartialFunction[api.Command, Option[Action]] = {
    case api.Move(botId, step) if "nsew".toSet.contains(step) =>
      val botMaybe = commander.bots
        .filter(_.hitpoints > 0)
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
        .filter(_.hitpoints > 0)
        .find(_.id == botId)
      val targetPos = Position(row=targetRow, col=targetCol)
      val isWithinMap = scenario.tilemap.isWithinMap(targetPos)
      for {
        bot <- botMaybe
        if isWithinMap
      } yield Attack(bot, targetPos)
  }
}
