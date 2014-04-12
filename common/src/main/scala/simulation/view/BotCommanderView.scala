package se.ramn.bottfarmen.simulation.view


trait BotCommanderView {
  val id: Int
  val name: String
  val bots: Iterable[BotView]
}
