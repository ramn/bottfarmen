package se.ramn.bottfarmen.example.wren

import collection.JavaConverters._
import collection.immutable.Seq

import se.ramn.bottfarmen.api.BotCommander
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot
import se.ramn.bottfarmen.example.BaseCommander


class WrenBot(var underlying: api.Bot) extends BaseBot {
  case class Pos(x: Int, y: Int)

  def isAlive = underlying.hitpoints > 0
  val directions = "nwse".toSeq
  def nextRandomDir = directions(util.Random.nextInt(directions.length))
  def update(gameState: GameState) = {
    val direction = 'e'
    val proposedPos = Pos(col+1, row)
    val targetTerrain = gameState.terrain.split("\n")(proposedPos.y)(proposedPos.x)
    if (targetTerrain == '.')
      Move(id, direction)
    else
      Move(id, 'n')
  }
}

class WrenCommander extends BaseCommander[WrenBot] {
  val name = "RandomCommander"

  override def makeBot(serverSideBot: api.Bot) = new WrenBot(serverSideBot)

  override def selectCommands(gameState: GameState): Seq[Command] = {
    val livingBots = bots.filter(_.isAlive)
    livingBots.toList.map { bot =>
      bot.update(gameState)
    }
  }
}
