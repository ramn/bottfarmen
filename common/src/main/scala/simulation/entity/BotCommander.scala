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
}


object BotCommander {
  def apply(
    playerBotCommander: api.BotCommander,
    id: Int
  ): BotCommander = {
      new BotCommanderImpl(id, playerBotCommander)
  }
}

class BotCommanderImpl(
  val id: Int,
  val playerCommander: api.BotCommander
) extends BotCommander {
  override val name = playerCommander.name
  override var bots = Set.empty[Bot]
  override var homeBase: Base = _
  override def requestCommands(gameState: api.GameState) = {
    playerCommander.update(gameState).asScala.toIndexedSeq
  }
}
