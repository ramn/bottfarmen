package se.ramn.bottfarmen.engine

import collection.immutable.Iterable
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.Bot
import impl.BotCommanderArbiterImpl


trait BotCommanderArbiter {
  def doTurn: Unit
  def bots: Iterable[RenderableBot]
}


object BotCommanderArbiter {
  def apply(commanders: Set[BotCommander]): BotCommanderArbiter = {
    new BotCommanderArbiterImpl(commanders)
  }
}
