package se.ramn.bottfarmen.example.basedefender

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.example.BaseCommander
import se.ramn.bottfarmen.example.BaseBot


class Bot(var underlying: api.Bot) extends BaseBot {
  def isAlive = underlying.hitpoints > 0

  def selectCommand(gameState: GameState): Option[Command] = {
    enemiesInSight.headOption.map { enemy =>
      api.Attack(id, row=enemy.row, col=enemy.col)
    }
  }
}


class BaseDefenderCommander extends BaseCommander[Bot] {
  val name = "BaseDefender"

  override def makeBot(serverSideBot: api.Bot) = new Bot(serverSideBot)

  override def selectCommands(gameState: GameState): Seq[Command] = {
    val livingBots = bots.filter(_.isAlive)
    livingBots.toList.flatMap { bot =>
      bot.selectCommand(gameState)
    }
  }
}
