package se.ramn.bottfarmen.example.idle

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.example.BaseCommander
import se.ramn.bottfarmen.example.BaseBot


class Bot(var underlying: api.Bot) extends BaseBot


class IdleCommander extends BaseCommander[Bot] {
  val name = "Idle"

  override def makeBot(serverSideBot: api.Bot) = new Bot(serverSideBot)

  override def selectCommands(gameState: GameState): Seq[Command] = {
    Seq.empty[Command]
  }
}
