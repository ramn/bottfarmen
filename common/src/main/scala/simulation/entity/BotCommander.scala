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
  override val name = playerCommander.name
  override var bots = Set.empty[Bot]
  override def requestCommands(gameState: api.GameState) = {
    playerCommander.update(gameState).asScala.toIndexedSeq
  }
}
