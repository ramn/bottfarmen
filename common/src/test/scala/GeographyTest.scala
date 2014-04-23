import collection.immutable.Seq
import org.scalatest.FunSuite
import org.scalatest.OneInstancePerTest
import org.scalamock.scalatest.MockFactory

import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.TileMap
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.Geography


class GeographyTest extends FunSuite {
  test("positionsWithinRange") {
    val target = Geography
    val range = 2
    val result = target.positionsWithinRange(Position(row=3, col=3), range)
    val p = Position
    val expected = Set(
      p(row=1, col=3),
      p(row=2, col=2), p(row=2, col=3), p(row=2, col=4),
      p(row=3, col=1), p(row=3, col=2), p(row=3, col=3), p(row=3, col=4), p(row=3, col=5),
      p(row=4, col=2), p(row=4, col=3), p(row=4, col=4),
      p(row=5, col=3)
    )
    assert(expected === result)
  }
}
