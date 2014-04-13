package se.ramn.bottfarmen.simulation

import collection.JavaConverters._
import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.entity.BotCommander
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

    val actionsForTurn = actions
    resolveMoveActions(actionsForTurn)
  }

  def resolveMoveActions(actions: Seq[Action]) = {
    val moveActions: Seq[Move] = actions.collect { case action: Move => action }
    val movers = moveActions.map(move => move.bot -> move.position).toMap
    val stillBots = commanders.flatMap(_.bots) -- movers.keySet
    val moveResolver = new MoveResolver(movers, stillBots, scenario)
    moveResolver.resolve()
  }

  def actions: Seq[Action] = {
    val commandsByCommander = extractCommands()
    val commanderCommandPairs: Seq[(BotCommander, api.Command)] = for {
      (commander, commands) <- commandsByCommander.toList
      command <- commands
    } yield (commander, command)
    val actions: Seq[Action] =
      commanderCommandPairs flatMap { case (commander, command) =>
        validateMove(commander)(command)
      }
    actions
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
}
