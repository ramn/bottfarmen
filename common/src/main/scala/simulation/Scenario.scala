package se.ramn.bottfarmen.simulation


trait Scenario {
  val tilemap: TileMap
  val maxFoodTilesCount: Int
  val maxBotCountPerCommander: Int
  val botVisibilityRange: Int
}
