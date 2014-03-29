package se.ramn.bottfarmen.example

import se.ramn.bottfarmen.api.Bot


trait MyBot {
  protected var underlying: Bot
  def id = underlying.id
  def setUnderlying(bot: Bot): Unit = {
    underlying = bot
  }
}

object MyBot {
  def updateBotsFromGameState(
    myBots: Iterable[MyBot],
    gameStateBots: Iterable[Bot],
    myBotFactory: Bot => MyBot
  ): Set[MyBot] = {
    val myBotsById = myBots.map(b => (b.id, b)).toMap
    gameStateBots.foreach { gameStateBot =>
      myBotsById.get(gameStateBot.id).foreach(_.setUnderlying(gameStateBot))
    }
    val newGameStateBots = gameStateBots.filterNot(b => myBotsById.keySet(b.id))
    val myNewBots = newGameStateBots.map(b => myBotFactory(b))
    val myOldBots = myBotsById.values.toSet
    myOldBots.toSet ++ myNewBots.toSet
  }
}
