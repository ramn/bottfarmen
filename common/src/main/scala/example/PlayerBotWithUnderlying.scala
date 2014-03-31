package se.ramn.bottfarmen.example

import collection.JavaConverters._
import se.ramn.bottfarmen.api.Bot


trait MyBot {
  protected var underlying: Bot
  def id = underlying.id
  def setUnderlying(bot: Bot): Unit = {
    underlying = bot
  }
  def enemiesInSight = underlying.enemiesInSight.asScala
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
    // drop bots that are removed serverside
    val myOldBots = myBotsById.values.filter { myBot =>
      gameStateBots.map(_.id).toSet(myBot.id)
    }
    myOldBots.toSet ++ myNewBots.toSet
  }
}
