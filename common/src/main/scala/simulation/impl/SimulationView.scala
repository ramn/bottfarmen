package se.ramn.bottfarmen.simulation.impl

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.BotView
import se.ramn.bottfarmen.simulation.BotCommanderView


trait ViewableSimulation {
  val commanderToId: Map[BotCommander, Int]
  def botsFor: Map[BotCommander, Set[Bot]]
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
