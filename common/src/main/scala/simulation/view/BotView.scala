package se.ramn.bottfarmen.simulation.view


trait BotView {
  val id: Int
  val commanderId: Int
  val row: Int
  val col: Int
  val hitpoints: Int
}
