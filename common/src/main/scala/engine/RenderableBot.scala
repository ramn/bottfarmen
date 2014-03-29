package se.ramn.bottfarmen.engine


trait RenderableBot {
  val id: Int
  val commanderId: Int
  def position: (Int, Int)
}
