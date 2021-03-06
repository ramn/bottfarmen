package se.ramn.bottfarmen.simulation.entity

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api


trait BotCommander {
  val id: Int
  val name: String
  var bots: Set[Bot]
  def requestCommands(gameState: api.GameState): Seq[api.Command]
  var homeBase: Base
  def spawnBot(atPosition: Position): Unit
}


object BotCommander {
  def apply(
    playerBotCommander: api.BotCommander,
    id: Int,
    homeBase: Base
  ): BotCommander = {
      new BotCommanderImpl(id, playerBotCommander, homeBase)
  }
}

class BotCommanderImpl(
  val id: Int,
  val playerCommander: api.BotCommander,
  var homeBase: Base
) extends BotCommander {
  val botIdsIter = Iterator.from(1)
  override val name = playerCommander.name
  override var bots = Set.empty[Bot]
  override def requestCommands(gameState: api.GameState) = {
    playerCommander.update(gameState).asScala.toIndexedSeq
  }
  override def toString: String = {
    s"BotCommander($id, $name)"
  }
  override def spawnBot(atPosition: Position) = {
    val bot = new Bot(botIdsIter.next, this) {
      var row = atPosition.row
      var col = atPosition.col
      var hitpoints = 100
    }
    bots += bot
  }
}
