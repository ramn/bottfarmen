package se.ramn.bottfarmen.simulation

import collection.immutable.Queue
import se.ramn.bottfarmen.simulation.entity.Position


object Geography extends Manhattan

class Manhattan {
  def positionsWithinRange(startPosition: Position, range: Int): Set[Position] = {
    var open = Queue(startPosition)
    var closed = Set.empty[Position]
    var distances = Map((startPosition) -> 0)
    while (!open.isEmpty) {
      val (tile, nextQueue) = open.dequeue
      open = nextQueue
      val currentDistance = distances(tile)
      val neighbourDistance = currentDistance + 1
      closed = closed + tile
      if (neighbourDistance <= range) {
        val currentNeighbours = tile.neighbours.filterNot(closed)
        open = nextQueue.enqueue(currentNeighbours)
        val neighboursWithDistances =
          currentNeighbours.zip(Stream.continually(currentDistance + 1))
        distances = distances ++ neighboursWithDistances
      }
    }
    distances.keySet
  }
}
