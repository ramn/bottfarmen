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
) extends Simulation {

  val commanderToId = commanders.zipWithIndex.toMap

  var botsByCommanderId: Map[Int, Set[Bot]] = Map()

  initialSetup()

  override def doTurn: Unit = {
    val commandsByCommander = commanders.map { commander =>
      val commands = commander.update(gameStateFor(commander))
      (commander -> commands)
    }
    // TODO: evaluate commands ...
  }

  override def botCommanders = commanders map commanderView

  override def bots = commanders flatMap botViewsForCommander

  def commanderView(commander: BotCommander): BotCommanderView = {
    new BotCommanderView {
      val id = commanderToId(commander)
      val name = commander.name
      val bots = botViewsForCommander(commander)
    }
  }

  def botViewsForCommander(commander: BotCommander): Iterable[BotView] = {
    val cmdrId = commanderToId(commander)
    botsByCommanderId(cmdrId) map { bot =>
      new BotView {
        val id = bot.id
        val commanderId = cmdrId
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
    }
  }

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
      setBotsFor(commanderToId(commander), Set(bot))
    }
  }

  protected def setBotsFor(commanderId: Int, bots: Set[Bot]) = {
    botsByCommanderId = botsByCommanderId.updated(commanderId, bots)
  }

  protected def gameStateFor(commander: BotCommander): GameState = {
    // TODO: build proper game state
    new GameState {
      def turn = 0
      def bots = botsByCommanderId(commanderToId(commander)).toList.asJava
    }
  }
}
