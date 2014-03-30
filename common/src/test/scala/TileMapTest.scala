import org.scalatest.FunSuite

import se.ramn.bottfarmen.engine.TileMap


class TileMapTest extends FunSuite {
  test("parse map") {
    /* Using different rows/cols counts than whats actually in the map to test
     * that we actually parse the header values, not counting rows.
     */
    val testMap = """# random_seed 3266485998379061860
      |# junk comment
      |players 2
      |rows 102
      |cols 129
      |m ...........
      |m ...........
      |m .........~.
      |m ........~..
      |m ~~.....~~..""".trim.stripMargin
    val result = TileMap.parse(testMap)
    assert(result.rowCount === 102)
    assert(result.colCount === 129)
    assert(result.rows.length === 5)
    assert(result.rows(0).forall(_ == '.'))
    assert(result.rows(0).length === 11)
    assert(result.rows(2)(9) === '~')
  }

  test("minimum viable map") {
    val testMap = """
      |rows 2
      |cols 3
      |m ...
      |m ~~.""".trim.stripMargin
    val result = TileMap.parse(testMap)
    assert(result.rowCount === 2)
    assert(result.colCount === 3)
    assert(result.rows.length === 2)
    assert(result.rows(0).forall(_ == '.'))
    assert(result.rows(0).length === 3)
    assert(result.rows(1)(0) === '~')
    assert(result.rows(1)(1) === '~')
    assert(result.rows(1)(2) === '.')
  }
}
