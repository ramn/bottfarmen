package se.ramn.bottfarmen.simulation.impl

import collection.JavaConverters._

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Bot
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.api.EnemyBot
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.BotCommanderView
import se.ramn.bottfarmen.simulation.BotView
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.Geography


class SimulationImpl(
  commanders: Set[BotCommander],
  scenario: Scenario
) extends Simulation with ViewableSimulation {

  val commanderToId = commanders.zipWithIndex.toMap
  lazy val view = new SimulationView(this)

  var botsFor: Map[BotCommander, Set[MutableBot]] = Map()

  initialSetup()

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
          val botMaybe = botsFor(commander).find(_.id == botId)
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
                  setBotsFor(commander, botsFor(commander) - bot)
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
      val commands = commander.update(gameStateFor(commander)).asScala
      (commander -> commands)
    }.toMap

  protected def initialSetup() = {
    require(commanders.size <= scenario.map.startingPositions.length)
    val startingPositions = scenario.map.startingPositions.iterator
    commanders foreach { commander =>
      val pos = startingPositions.next
      val bot = new MutableBot(1) {
        var row = pos.row
        var col = pos.col
        var hitpoints = 100
      }
      setBotsFor(commander, Set(bot))
    }
  }

  protected def setBotsFor(commander: BotCommander, bots: Set[MutableBot]) = {
    botsFor = botsFor.updated(commander, bots)
  }

  protected def gameStateFor(commander: BotCommander): GameState = {
    val immutableBots: Seq[Bot] = botsFor(commander).toList.map { bot =>
      val otherCommanders = commanders.filterNot(_ == commander)
      val visibleTiles = Geography.positionsWithinRange(bot.position, range=5)
      val visibleEnemyBots = for {
        commander <- otherCommanders
        bot <- botsFor(commander)
        if visibleTiles(bot.position)
      } yield new EnemyBot {
        val commanderId = commanderToId(commander)
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
      new Bot {
        val id = bot.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
        val enemiesInSight = visibleEnemyBots.toList.asJava
      }
    }
    new GameState {
      def turn = 0
      def bots = immutableBots.asJava
      def terrain = scenario.map.rows.map(_.mkString).mkString("\n")
      def rowCount = scenario.map.rowCount
      def colCount = scenario.map.colCount
    }
  }
}


abstract class MutableBot(val id: Int) {
  var row: Int
  var col: Int
  var hitpoints: Int
  def position = Position(row=row, col=col)
}


trait ViewableSimulation {
  val commanderToId: Map[BotCommander, Int]
  def botsFor: Map[BotCommander, Set[MutableBot]]
}


class SimulationView(viewableSimulation: ViewableSimulation) {
  protected lazy val commanders = commanderToId.keySet
  protected lazy val commanderToId = viewableSimulation.commanderToId

  def botCommanders = commanders map commanderView

  def bots = commanders flatMap botViewsForCommander

  protected def botsFor = viewableSimulation.botsFor

  protected def commanderView(commander: BotCommander): BotCommanderView = {
    new BotCommanderView {
      val id = commanderToId(commander)
      val name = commander.name
      val bots = botViewsForCommander(commander)
    }
  }

  protected def botViewsForCommander(commander: BotCommander): Iterable[BotView] = {
    botsFor(commander) map { bot =>
      new BotView {
        val id = bot.id
        val commanderId = commanderToId(commander)
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
    }
  }
}
