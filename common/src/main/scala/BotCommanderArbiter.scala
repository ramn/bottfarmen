package se.ramn.bottfarmen

import api.BotCommander
import api.GameState
import api.Bot


class BotCommanderArbiter(commanders: Set[BotCommander]) {
  def update: Unit = {
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
