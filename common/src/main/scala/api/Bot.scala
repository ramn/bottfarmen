package se.ramn.bottfarmen.api


trait Bot {
  val id: Int
  def position: (Int, Int)
  def hitpoints: Int
}
