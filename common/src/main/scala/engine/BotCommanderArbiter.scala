package se.ramn.bottfarmen.engine

import collection.immutable.Iterable
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.Bot
import impl.BotCommanderArbiterImpl


trait BotCommanderArbiter {
  def doTurn: Unit
  def botCommanders: Iterable[BotCommanderView]
  def bots: Iterable[BotView]
}


object BotCommanderArbiter {
  def apply(
    commanders: Set[BotCommander],
    scenario: Scenario
  ): BotCommanderArbiter = {
    new BotCommanderArbiterImpl(commanders, scenario)
  }
}
