package se.ramn.bottfarmen.example.baseraider

import collection.immutable.Seq
import util.Random

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.api.GameState
import se.ramn.bottfarmen.api.Command
import se.ramn.bottfarmen.api.Move
import se.ramn.bottfarmen.example.BaseBot
import se.ramn.bottfarmen.util.Logging
import se.ramn.bottfarmen.util.Timer.time


sealed trait Task
case class FollowPath(
  remainingPath: Seq[Position],
  expectedPos: Position) extends Task


class Bot(var underlying: api.Bot) extends BaseBot with Logging {
  var taskStack = List.empty[Task]
  def isAlive = underlying.hitpoints > 0
  def position = Position(row=row, col=col)

  def selectCommand(gameState: GameState): Option[Command] = {
    val terrain = new Terrain(gameState)
    issueTasks(gameState, terrain)
    commandOptFromTaskStack orElse defaultCommandOpt(terrain)
  }

  def issueTasks(gameState: GameState, terrain: Terrain): Unit = {
    if (gameState.turn == 1) {
      val pathToEnemyBase = findPathToEnemyBase(terrain)
      if (!pathToEnemyBase.isEmpty) {
        taskStack +:= FollowPath(pathToEnemyBase, position)
      }
    }
  }

  def defaultCommandOpt(terrain: Terrain): Option[Command] = {
    if (position == terrain.enemyBasePos)
      Some(api.Attack(id, row=row, col=col))
    else
      pickRandomMove(terrain)
  }

  def commandOptFromTaskStack: Option[Command] = taskStack match {
    case FollowPath(steps, expectedPos) :: taskTail =>
      if (expectedPos == position) {
        steps match {
          case nextStep :: stepTail =>
            taskStack = FollowPath(stepTail, nextStep) :: taskTail
            val moveCommand = Move(id, neighbourToDirection(nextStep))
            Some(moveCommand)
          case Nil =>
            taskStack = taskTail
            None
        }
      } else if (position.neighbours(expectedPos)) {
        val moveCommand = Move(id, neighbourToDirection(expectedPos))
        Some(moveCommand)
      } else {
        taskStack = taskTail
        None
      }
    case _ => None
  }

  def pickRandomMove(terrain: Terrain): Option[Move] = {
    val walkableNeighbours = position.neighbours.filter(terrain.isWalkable)
    def nextRandomDirOpt = Random.shuffle(walkableNeighbours)
      .map(neighbourToDirection)
      .headOption
    nextRandomDirOpt map (Move(id, _))
  }

  def findPathToEnemyBase(terrain: Terrain) = {
    logger.debug(s"Will try to find path from $position to ${terrain.enemyBasePos}")
    val pathToEnemyBase = time(
      "pathfinding enemy base",
      terrain.findPath(position, terrain.enemyBasePos))
    pathToEnemyBase
  }

  def neighbourToDirection(position: Position): Char = {
    if (position.row < row) 'n'
    else if (position.row > row) 's'
    else if (position.col < col) 'w'
    else 'e'
  }
}
