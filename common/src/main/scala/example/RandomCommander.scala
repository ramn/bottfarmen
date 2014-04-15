package se.ramn.bottfarmen.example

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move


class RandomCommanderBot(var underlying: api.Bot) extends BaseBot {
  def isAlive = underlying.hitpoints > 0
}

class RandomCommander extends BaseCommander[RandomCommanderBot] {
  val name = "RandomCommander"

  override def makeBot(serverSideBot: api.Bot) =
    new RandomCommanderBot(serverSideBot)

  override def selectCommands(gameState: GameState): Seq[Command] = {
    val livingBots = bots.filter(_.isAlive)
    val directions = "nwse".toSeq
    def nextRandomDir = directions(util.Random.nextInt(directions.length))
    livingBots.toList.map { bot =>
      Move(bot.id, nextRandomDir)
    }
  }
}
