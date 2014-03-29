import org.scalatest.FunSuite

import se.ramn.bottfarmen.util.Times


class UtilTest extends FunSuite {
  test("should run 0 timesDo") {
    var count = 0
    0 timesDo { count += 1 }
    assert(count === 0)
  }

  test("should run 1 timesDo") {
    var count = 0
    1 timesDo { count += 1 }
    assert(count === 1)
  }

  test("should throw on negative input") {
    intercept[IllegalArgumentException] {
      -1 timesDo {}
    }
  }
}
