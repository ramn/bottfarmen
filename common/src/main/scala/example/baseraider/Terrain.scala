package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq

import se.ramn.bottfarmen.api.GameState


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
