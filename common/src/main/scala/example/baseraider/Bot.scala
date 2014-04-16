package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq
import util.Random

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot


sealed trait Task
case class FollowPath(remainingPath: Seq[Position]) extends Task


class Bot(var underlying: api.Bot) extends BaseBot {
  var taskStack = List.empty[Task]
  def isAlive = underlying.hitpoints > 0
  def position = Position(row=row, col=col)

  def selectCommand(gameState: GameState): Option[Command] = {
    val terrain = new Terrain(gameState)
    if (gameState.turn == 1) {
      val pathToEnemyBase = findPathToEnemyBase(terrain)
      taskStack +:= FollowPath(pathToEnemyBase)
    }
    taskStack match {
      case FollowPath(nextStep :: stepTail) :: taskTail =>
        taskStack = FollowPath(stepTail) :: taskTail
        val moveCommand = Move(id, neighbourToDirection(nextStep))
        Some(moveCommand)
      case FollowPath(Nil) :: taskTail =>
        taskStack = taskTail
        pickRandomMove(terrain)
      case _ => pickRandomMove(terrain)
    }
  }

  def pickRandomMove(terrain: Terrain): Option[Move] = {
    val walkableNeighbours = position.neighbours.filter(terrain.isWalkable)
    def nextRandomDirOpt = Random.shuffle(walkableNeighbours)
      .map(neighbourToDirection)
      .headOption
    nextRandomDirOpt map (Move(id, _))
  }

  def findPathToEnemyBase(terrain: Terrain) = {
    println(s"Will try to find path from $position to ${terrain.enemyBasePos}")
    val start = System.currentTimeMillis
    val pathToEnemyBase = terrain.findPath(position, terrain.enemyBasePos)
    val duraionMs = (System.currentTimeMillis - start)
    println(s"Took $duraionMs ms to pathfind enemy base")
    //pathToEnemyBase foreach println
    pathToEnemyBase
  }

  def neighbourToDirection(position: Position): Char = {
    if (position.row < row) 'n'
    else if (position.row > row) 's'
    else if (position.col < col) 'w'
    else 'e'
  }
}
