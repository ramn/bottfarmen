package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq
import collection.immutable.Queue

import se.ramn.bottfarmen.api.GameState


class Terrain(gameState: GameState) {
  val rowCount = gameState.rowCount
  val colCount = gameState.colCount
  lazy val terrainGrid = gameState.terrain.split("\n").toIndexedSeq
  lazy val homeBasePos =
    Position(row=gameState.homeBase.row, col=gameState.homeBase.col)
  lazy val enemyBasePos: Position =
    allPositions
      .filter(_ != homeBasePos)
      .filter(pos => tile(pos).filter("0123456789".toSet).isDefined)
      .head

  def isWithinMap(position: Position): Boolean = {
    val (row, col) = (position.row, position.col)
    rowCount > row && row >= 0 && colCount > col && col >= 0
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

  val allPositions: Set[Position] = for {
    rowIx <- (0 until rowCount).toSet[Int]
    colIx <- 0 until colCount
  } yield Position(row=rowIx, col=colIx)

  def findPath(source: Position, target: Position): Seq[Position] = {
    require(isWithinMap(target))
    type Node = Position
    val graph = new Graph[Node] {
      def neighbours(node: Node): Set[Node] = node.neighbours.filter(isWalkable)
      def costFor(node: Node): Cost = 1
      val nodes: Set[Node] = allPositions.filter(isWalkable)
      def heuristicCostToGoalFrom(node: Node): Cost = {
        math.abs(target.col - node.col) + math.abs(target.row - node.row)
      }
      val maxCost: Cost = Int.MaxValue
      def isGoal(node: Node): Boolean = node == target
    }
    val pathfinder = Pathfinder(source, graph)
    pathfinder.path
  }
}
