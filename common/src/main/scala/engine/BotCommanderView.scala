package se.ramn.bottfarmen.simulation


trait BotCommanderView {
  val id: Int
  val name: String
  val bots: Iterable[BotView]
}
