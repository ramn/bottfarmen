package se.ramn.bottfarmen.simulation.impl

import collection.JavaConverters._
import collection.immutable.Iterable
import collection.immutable.Seq
import collection.immutable.IndexedSeq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.GameStateApiGateway
import se.ramn.bottfarmen.simulation.MoveResolver
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.entity.BotCommander


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
              scenario.map.rowCount >= targetRow
              && targetRow >= 0
              && scenario.map.colCount >= targetCol
              && targetCol >= 0)
            val targetTile = scenario.map.rows(targetRow)(targetCol)
            val isWalkable = targetTile != '^' // can't walk on mountains
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

    val actions = actionMaybes.flatten
    val moveActions: Seq[Goto] = actions.collect { case action: Goto => action }
    val desiredPositionByBot = moveActions.map(move => move.bot -> move.position).toMap
    val movingBots = desiredPositionByBot.keySet
    val stillBots = commanders.flatMap(_.bots) -- movingBots
    val moveResolver = new MoveResolver(desiredPositionByBot, stillBots, scenario)
    moveResolver.resolve()
  }

  def extractCommands: Map[BotCommander, Seq[api.Command]] =
    commanders.map { commander =>
      val commands = commander.requestCommands(gameStateFor(commander))
      (commander -> commands)
    }.toMap

  protected def gameStateFor(commander: BotCommander): api.GameState =
    gameStateApiGateway.forCommander(commander)
}
