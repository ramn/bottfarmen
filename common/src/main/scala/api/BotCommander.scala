package se.ramn.bottfarmen.api


trait BotCommander {
  def name: String
  def update(gameState: GameState): java.util.List[Command]
}
