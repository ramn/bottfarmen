package se.ramn.bottfarmen.simulation.impl

import collection.JavaConverters._

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.api.EnemyBot
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.Geography
import se.ramn.bottfarmen.simulation.entity.BotCommander


class SimulationImpl(
  val commanders: Set[BotCommander],
  scenario: Scenario
) extends Simulation {
  lazy val view = new SimulationView(commanders)

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

  def extractCommands: Map[BotCommander, Seq[Command]] =
    commanders.map { commander =>
      val commands = commander.requestCommands(gameStateFor(commander))
      (commander -> commands)
    }.toMap

  protected def gameStateFor(commander: BotCommander): GameState = {
    val immutableBots: Seq[api.Bot] = commander.bots.toList.map { bot =>
      val otherCommanders = commanders.filterNot(_ == commander)
      val visibleTiles = Geography.positionsWithinRange(bot.position, range=5)
      val visibleEnemyBots = for {
        commander <- otherCommanders
        bot <- commander.bots
        if visibleTiles(bot.position)
      } yield new EnemyBot {
        val commanderId = commander.id
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
      new api.Bot {
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
        val enemiesInSight = visibleEnemyBots.toList.asJava
      }
    }
    new GameState {
      val turn = 0
      val bots = immutableBots.asJava
      val terrain = scenario.map.rows.map(_.mkString).mkString("\n")
      val rowCount = scenario.map.rowCount
      val colCount = scenario.map.colCount
      val homeBase = apiBaseFrom(commander)
    }
  }

  protected def apiBaseFrom(commander: BotCommander): api.Base =
    new api.Base {
      val hitpoints = commander.homeBase.hitpoints
      val row = commander.homeBase.row
      val col = commander.homeBase.col
    }
}
