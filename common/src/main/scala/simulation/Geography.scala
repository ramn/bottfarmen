package se.ramn.bottfarmen.simulation

import collection.immutable.Queue
import se.ramn.bottfarmen.simulation.entity.Position


object Geography extends Manhattan

class Manhattan {
  def positionsWithinRange(startPosition: Position, range: Int): Set[Position] = {
    val s = startPosition
    val halfRange = (range - 1) to 0 by -1

    val aboveOrBelow = for {
      radius <- halfRange
      rowUp = (range - radius)
      col <- (-radius to radius by 1)
    } yield Set(
      Position(row=s.row-rowUp, col=s.col+col),
      Position(row=s.row+rowUp, col=s.col+col))

    val same = for {
      radius <- (-range to range)
    } yield s.copy(col=s.col+radius)

    aboveOrBelow.flatten.toSet ++ same.toSet
  }
}
