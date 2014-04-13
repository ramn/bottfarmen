package se.ramn.bottfarmen.simulation.view

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.BotCommander


class SimulationView(commanders: Set[BotCommander]) {

  def botCommanders = commanders map commanderView

  def bots = commanders flatMap botViewsForCommander

  protected def commanderView(commander: BotCommander): BotCommanderView = {
    val base = commander.homeBase
    new BotCommanderView {
      val id = commander.id
      val name = commander.name
      val bots = botViewsForCommander(commander)
      val homeBase = new BaseView {
        override val hitpoints = base.hitpoints
        override val row = base.row
        override val col = base.col
      }
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
