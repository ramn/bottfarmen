package se.ramn.bottfarmen.example

import collection.JavaConverters._
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Bot
import se.ramn.bottfarmen.api.Command


class DummyBot(initialUnderlying: Bot) extends MyBot {
  protected override var underlying = initialUnderlying
}

class DummyBotCommander extends BotCommander {
  val name = "Dummy bot commander"
  protected var bots = Set.empty[MyBot]

  def update(gameState: GameState) = {
    updateMyBotsFromGameState(gameState)
    val commands = java.util.Collections.emptyList[Command]
    commands
  }

  def updateMyBotsFromGameState(gameState: GameState) = {
    bots = MyBot.updateBotsFromGameState(
      bots,
      gameState.bots.asScala,
      bot => { new DummyBot(bot) })
  }
}
