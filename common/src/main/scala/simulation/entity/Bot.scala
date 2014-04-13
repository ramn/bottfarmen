package se.ramn.bottfarmen.simulation.entity


abstract class Bot(val id: Int, val commander: BotCommander) {
  var row: Int
  var col: Int
  var hitpoints: Int
  def position = Position(row=row, col=col)
  val attackStrength = 40

  override def toString: String = {
    s"Bot($id, C${commander.id}, HP$hitpoints, $position)"
  }

  def takeDamage(damage: Int) = {
    hitpoints -= damage
    if (hitpoints <= 0) {
      commander.bots -= this
    }
  }
}
