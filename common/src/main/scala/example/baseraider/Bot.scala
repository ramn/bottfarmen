package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq

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

class Bot(var underlying: api.Bot) extends BaseBot {
  def isAlive = underlying.hitpoints > 0
  def position = Position(row=row, col=col)

  def issueCommand(gameState: GameState): Command = {
    object Terrain {
      val rowCount = gameState.rowCount
      val colCount = gameState.colCount
      val terrainGrid = gameState.terrain.split("\n").toIndexedSeq
      def isWithinMap(position: Position): Boolean = {
        val (row, col) = (position.row, position.col)
        rowCount >= row && row >= 0 && colCount >= col && col >= 0
      }
      def tile(position: Position): Char = {
        terrainGrid(position.row)(position.col)
      }
    }
    val walkableNeighbours = position.neighbours
      .filter(Terrain.isWithinMap)
      .filter(pos => Terrain.tile(pos) != '~')

    val directions = "nwse".toSeq
    def nextRandomDir = directions(util.Random.nextInt(directions.length))
    Move(id, nextRandomDir)
  }
}
