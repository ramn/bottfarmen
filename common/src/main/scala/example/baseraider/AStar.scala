package se.ramn.bottfarmen.example.baseraider

import annotation.tailrec
import collection.immutable.Seq
import collection.immutable.Queue


trait Pathfinder[Node] {
  def path: Seq[Node]
  def goal: Option[Node]
}


object Pathfinder {
  def apply[Node](start: Node, graph: Graph[Node]): Pathfinder[Node] = {
    new AStar(start, graph)
  }
}


trait Graph[Node] {
  type Cost = Int

  def neighbours(node: Node): Set[Node]

  def costFor(node: Node): Cost

  def nodes: Set[Node]

  def heuristicCostToGoalFrom(node: Node): Cost

  def maxCost: Cost

  def isGoal(node: Node): Boolean
}


class AStar[Node](start: Node, graph: Graph[Node]) extends Pathfinder[Node] {

  val initialDistances: Map[Node, Int] = {
    for {
      node <- graph.nodes
      cost =
        if (node == start) graph.costFor(node)
        else graph.maxCost
    } yield node -> cost
  }.toMap

  @tailrec
  private def calcDistances(
    open: Queue[Node],
    closed: Set[Node],
    distances: Map[Node, Int]
  ): (Map[Node, Int], Option[Node]) = {
    if (open.isEmpty) {
      (distances, None)
    } else {
      def dequeueNodeClosestByHeuristic = {
        open.sortBy(graph.heuristicCostToGoalFrom).dequeue
      }
      val (current, openRest) = dequeueNodeClosestByHeuristic
      if (graph.isGoal(current)) {
        (distances, Some(current))
      } else {
        val openNeighbours = graph.neighbours(current) filterNot closed
        val updatedOpen = openNeighbours.foldLeft(openRest) { (open, node) =>
          open.enqueue(node)
        }
        val updatedDistances =
          calcUpdatedDistances(current, openNeighbours, distances)
        val updatedClosed = closed + current
        calcDistances(updatedOpen, updatedClosed, updatedDistances)
      }
    }
  }

  def calcUpdatedDistances(
    current: Node,
    openNeighbours: Set[Node],
    distances: Map[Node, Int]
  ) = {
    openNeighbours.foldLeft(distances) { (distances, node) =>
      val newDistance = distances(current) + graph.costFor(current)
      if (distances(node) > newDistance) {
        distances.updated(node, newDistance)
      } else {
        distances
      }
    }
  }

  lazy val (distances, goal) = calcDistances(Queue(start), Set.empty, initialDistances)
  lazy val path: Seq[Node] = calcPath(distances, goal)

  def calcPath(distances: Map[Node, Int], goal: Option[Node]): Seq[Node] = {
    def traverse(current: Node, path: List[Node]): Seq[Node] = {
      if (current == start) {
        path
      } else {
        val next = graph.neighbours(current).minBy(distances)
        traverse(next, next :: path)
      }
    }
    goal.toList flatMap { goal =>
      val inOrderFromStartToGoal = traverse(goal, List(goal)).tail
      inOrderFromStartToGoal
    }
  }
}
