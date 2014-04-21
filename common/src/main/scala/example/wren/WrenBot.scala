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

  var gameState: GameState = null
  var myPos: Pos = null

  def enemyInRange: Boolean = {
    println(enemiesInSight)
    true
  }

  def possibleMoves = Map(
    (Pos(myPos.x+1, myPos.y), 'e'),
    (Pos(myPos.x-1, myPos.y), 'w'),
    (Pos(myPos.x, myPos.y-1), 'n'),
    (Pos(myPos.x, myPos.y+1), 's')
  )

  def moveIsValid(newPos: Pos) = {
    if(gameState.terrain.split("\n")(newPos.y)(newPos.x) != '~')
      true
    else
      false
  }

  def validMoves = possibleMoves.filter(p => moveIsValid(p._1)).map(_._2)

  def isAlive = underlying.hitpoints > 0

  def nextRandomDir = validMoves.toSeq(util.Random.nextInt(validMoves.toSeq.length))

  def update(gameState: GameState) = {
    myPos = Pos(col, row)
    this.gameState = gameState
    enemyInRange
    println(validMoves.toString())
    Move(id, nextRandomDir)
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
