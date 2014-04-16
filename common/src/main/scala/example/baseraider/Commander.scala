package se.ramn.bottfarmen.example.baseraider

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot
import se.ramn.bottfarmen.example.BaseCommander


class Commander extends BaseCommander[Bot] {
  val name = "BaseRaider"

  override def makeBot(serverSideBot: api.Bot) = new Bot(serverSideBot)

  override def selectCommands(gameState: GameState): Seq[Command] = {
    val livingBots = bots.filter(_.isAlive)
    val directions = "nwse".toSeq
    def nextRandomDir = directions(util.Random.nextInt(directions.length))
    livingBots.toList.map { bot =>
      Move(bot.id, nextRandomDir)
    }
  }
}
