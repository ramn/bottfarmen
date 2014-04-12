package se.ramn.bottfarmen.simulation.view

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.BotView
import se.ramn.bottfarmen.simulation.BotCommanderView


class SimulationView(commanders: Set[BotCommander]) {

  def botCommanders = commanders map commanderView

  def bots = commanders flatMap botViewsForCommander

  protected def commanderView(commander: BotCommander): BotCommanderView = {
    new BotCommanderView {
      val id = commander.id
      val name = commander.name
      val bots = botViewsForCommander(commander)
    }
  }

  protected def botViewsForCommander(commander: BotCommander): Iterable[BotView] = {
    commander.bots map { bot =>
      new BotView {
        val id = bot.id
        val commanderId = commander.id
        val row = bot.row
        val col = bot.col
        val hitpoints = bot.hitpoints
      }
    }
  }
}
