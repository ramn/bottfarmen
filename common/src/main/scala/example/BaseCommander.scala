package se.ramn.bottfarmen.example

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.EnemyBot
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move


trait BaseBot {
  var underlying: api.Bot

  def id: Int = underlying.id
  def row: Int = underlying.row
  def col: Int = underlying.col
  def hitpoints: Int = underlying.hitpoints
  def enemiesInSight: Set[EnemyBot] = underlying.enemiesInSight.asScala.toSet
}


trait BaseCommander[Bot <: BaseBot] extends BotCommander {
  val name: String

  protected var bots = Set.empty[Bot]

  def selectCommands(gameState: GameState): Seq[Command]

  def makeBot(serverSideBot: api.Bot): Bot

  def update(gameState: GameState) = {
    updateMyBotsFromGameState(gameState)
    selectCommands(gameState).asJava
  }

  protected def updateMyBotsFromGameState(gameState: GameState) = {
    bots = updateMyBots(bots, gameState.bots.asScala, (makeBot _))
  }

  protected def updateMyBots(
    myBots: Iterable[Bot],
    gameStateBots: Iterable[api.Bot],
    myBotFactory: api.Bot => Bot
  ): Set[Bot] = {
    val myBotsById = myBots.map(b => (b.id, b)).toMap
    gameStateBots.foreach { gameStateBot =>
      myBotsById.get(gameStateBot.id).foreach(_.underlying = gameStateBot)
    }
    val newGameStateBots = gameStateBots.filterNot(b => myBotsById.keySet(b.id))
    val myNewBots = newGameStateBots.map(b => myBotFactory(b))
    // Keep dead bots
    myBots.toSet ++ myNewBots.toSet
  }
}
