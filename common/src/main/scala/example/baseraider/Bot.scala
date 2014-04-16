package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq
import util.Random

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot


case class Position(row: Int, col: Int) {
  def neighbours: Set[Position] =
    Set(
      copy(row=row + 1),
      copy(row=row - 1),
      copy(col=col + 1),
      copy(col=col - 1))
}


class Terrain(gameState: GameState) {
  val rowCount = gameState.rowCount
  val colCount = gameState.colCount
  val terrainGrid = gameState.terrain.split("\n").toIndexedSeq

  def isWithinMap(position: Position): Boolean = {
    val (row, col) = (position.row, position.col)
    rowCount >= row && row >= 0 && colCount >= col && col >= 0
  }

  def tile(position: Position): Option[Char] = {
    if (isWithinMap(position))
      Some(terrainGrid(position.row)(position.col))
    else
      None
  }

  def isWalkable(position: Position): Boolean = {
    val nonWalkable = Set('~')
    tile(position).filterNot(nonWalkable).isDefined
  }
}


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
