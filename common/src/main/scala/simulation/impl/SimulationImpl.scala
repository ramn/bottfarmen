package se.ramn.bottfarmen.simulation.impl

import collection.JavaConverters._

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.GameStateApiGateway
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
    for {
      (commander, commands) <- commandsByCommander
      command <- commands
    } {
      command match {
        case Move(botId, steps) if !steps.isEmpty =>
          val botMaybe = commander.bots.find(_.id == botId)
          botMaybe foreach { bot =>
            // TODO: handle more than one step
            val step = steps.filter("nsew".toSet).head
            val (targetRow, targetCol) = step match {
              case 'n' => (bot.row - 1) -> bot.col
              case 's' => (bot.row + 1) -> bot.col
              case 'w' => bot.row -> (bot.col - 1)
              case 'e' => bot.row -> (bot.col + 1)
            }
            val isWithinMap = (scenario.map.rowCount >= targetRow
              && targetRow >= 0
              && scenario.map.colCount >= targetCol
              && targetCol >= 0)
            if (isWithinMap) {
              val targetTile = scenario.map.rows(targetRow)(targetCol)
              targetTile match {
                case '~' =>
                  commander.bots -= bot
                case _ =>
                  bot.row = targetRow
                  bot.col = targetCol
              }
            }
          }
      }
    }
  }

  def extractCommands: Map[BotCommander, Seq[api.Command]] =
    commanders.map { commander =>
      val commands = commander.requestCommands(gameStateFor(commander))
      (commander -> commands)
    }.toMap

  protected def gameStateFor(commander: BotCommander): api.GameState =
    gameStateApiGateway.forCommander(commander)
}
