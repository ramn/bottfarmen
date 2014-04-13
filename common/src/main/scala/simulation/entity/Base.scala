package se.ramn.bottfarmen.simulation.entity


class Base(
  var hitpoints: Int,
  val row: Int,
  val col: Int
) {
  lazy val position = Position(row=row, col=col)

  def takeDamage(damage: Int): Unit = {
    hitpoints -= damage
  }
}
