package se.ramn.bottfarmen

import api.BotCommander
import api.GameState
import api.Bot


trait BotCommanderArbiter {
  def doTurn: Unit
}


class BotCommanderArbiterImpl(commanders: Set[BotCommander])
  extends BotCommanderArbiter {

  def doTurn: Unit = {
    val commandsByCommander = commanders.map { commander =>
      val commands = commander.update(gameStateFor(commander))
      (commander -> commands)
    }
    // evaluate commands ...
  }

  def gameStateFor(commander: BotCommander): GameState = {
    new GameState {
      def turn = 0
      def bots = java.util.Collections.emptyList[Bot]
    }
  }
}


object BotCommanderArbiter {
  def apply(commanders: Set[BotCommander]): BotCommanderArbiter = {
    new BotCommanderArbiterImpl(commanders)
  }
}
