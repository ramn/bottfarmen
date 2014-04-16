package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq
import util.Random

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot


class Bot(var underlying: api.Bot) extends BaseBot {
  def isAlive = underlying.hitpoints > 0
  def position = Position(row=row, col=col)

  def selectCommand(gameState: GameState): Option[Command] = {
    val terrain = new Terrain(gameState)
    val walkableNeighbours = position.neighbours.filter(terrain.isWalkable)

    def nextRandomDirOpt = Random.shuffle(walkableNeighbours)
      .map(neighbourToDirection)
      .headOption
    nextRandomDirOpt map (Move(id, _))
  }

  def neighbourToDirection(position: Position): Char = {
    if (position.row < row)
      'n'
    else if (position.row > row)
      's'
    else if (position.col < col)
      'w'
    else
      'e'
  }
}
