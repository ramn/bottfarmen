package se.ramn.bottfarmen.simulation.entity


abstract class Bot(val id: Int, val commander: BotCommander) {
  var row: Int
  var col: Int
  var hitpoints: Int
  def position = Position(row=row, col=col)
  val attackStrength = 40
}
