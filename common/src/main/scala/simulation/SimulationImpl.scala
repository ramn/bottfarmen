package se.ramn.bottfarmen.simulation

import collection.JavaConverters._
import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Move
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

  override def botCommanders = view.botCommanders

  override def bots = view.bots

  override def doTurn: Unit = {
    val commandsByCommander = extractCommands

    trait Action
    case class Goto(bot: Bot, position: Position) extends Action

    val actionMaybes: Seq[Option[Action]] = for {
      (commander, commands) <- commandsByCommander.toIndexedSeq
      command <- commands
    } yield {
      command match {
        case Move(botId, steps) if !steps.isEmpty =>
          val botMaybe = commander.bots
            .filter(_.hitpoints > 0)
            .find(_.id == botId)
          val maybeAction = botMaybe flatMap { bot =>
            // TODO: handle more than one step
            val step = steps.filter("nsew".toSet).head
            val (targetRow, targetCol) = step match {
              case 'n' => (bot.row - 1) -> bot.col
              case 's' => (bot.row + 1) -> bot.col
              case 'w' => bot.row -> (bot.col - 1)
              case 'e' => bot.row -> (bot.col + 1)
            }
            val isWithinMap = (
              scenario.tilemap.rowCount >= targetRow
              && targetRow >= 0
              && scenario.tilemap.colCount >= targetCol
              && targetCol >= 0)
            val targetTileOpt = scenario.tilemap.tile(Position(row=targetRow, col=targetCol))
            val mountain = '^'
            val isWalkable = targetTileOpt.map(_ != mountain).getOrElse(false)
            if (isWithinMap && isWalkable) {
              Some(Goto(bot, Position(row=targetRow, col=targetCol)))
            } else {
              None
            }
          }
          // return eligible moves
          maybeAction
        case _ => None
      }
    }

    val validMoveActions: Seq[Goto] =
      actionMaybes.flatten.collect { case action: Goto => action }
    val movers = validMoveActions.map(move => move.bot -> move.position).toMap
    val stillBots = commanders.flatMap(_.bots) -- movers.keySet
    val moveResolver = new MoveResolver(movers, stillBots, scenario)
    moveResolver.resolve()
  }

  def extractCommands: Map[BotCommander, Seq[api.Command]] =
    commanders.map { commander =>
      val commands = commander.requestCommands(gameStateFor(commander))
      (commander -> commands)
    }.toMap

  def gameStateFor(commander: BotCommander): api.GameState =
    gameStateApiGateway.forCommander(commander)
}
