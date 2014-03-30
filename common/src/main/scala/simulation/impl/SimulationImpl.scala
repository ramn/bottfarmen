package se.ramn.bottfarmen.simulation.impl

import collection.JavaConverters._

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Bot
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.BotCommanderView
import se.ramn.bottfarmen.simulation.BotView
import se.ramn.bottfarmen.simulation.Scenario


class SimulationImpl(
  commanders: Set[BotCommander],
  scenario: Scenario
) extends Simulation with ViewableSimulation {

  val commanderToId = commanders.zipWithIndex.toMap
  lazy val view = new SimulationView(this)

  var botsByCommander: Map[BotCommander, Set[Bot]] = Map()

  initialSetup()

  override def doTurn: Unit = {
    val commandsByCommander = commanders.map { commander =>
      val commands = commander.update(gameStateFor(commander))
      (commander -> commands)
    }
    // TODO: evaluate commands ...
  }

  override def botCommanders = view.botCommanders

  override def bots = view.bots

  protected def initialSetup() = {
    require(commanders.size <= scenario.map.startingPositions.length)
    val startingPositions = scenario.map.startingPositions.iterator
    commanders foreach { commander =>
      val pos = startingPositions.next
      val bot = new Bot {
        val id = 1
        val row = pos.row
        val col = pos.col
        def hitpoints = 100
      }
      setBotsFor(commander, Set(bot))
    }
  }

  protected def setBotsFor(commander: BotCommander, bots: Set[Bot]) = {
    botsByCommander = botsByCommander.updated(commander, bots)
  }

  protected def gameStateFor(commander: BotCommander): GameState = {
    // TODO: build proper game state
    new GameState {
      def turn = 0
      def bots = botsByCommander(commander).toList.asJava
    }
  }
}


trait ViewableSimulation {
  val commanderToId: Map[BotCommander, Int]
  def botsByCommander: Map[BotCommander, Set[Bot]]
}


class SimulationView(viewableSimulation: ViewableSimulation) {
  protected val commanders = commanderToId.keySet
  protected val commanderToId = viewableSimulation.commanderToId

  def botCommanders = commanders map commanderView

  def bots = commanders flatMap botViewsForCommander

  protected def botsByCommander = viewableSimulation.botsByCommander

  protected def commanderView(commander: BotCommander): BotCommanderView = {
    new BotCommanderView {
      val id = commanderToId(commander)
      val name = commander.name
      val bots = botViewsForCommander(commander)
    }
  }

  protected def botViewsForCommander(commander: BotCommander): Iterable[BotView] = {
    botsByCommander(commander) map { bot =>
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
