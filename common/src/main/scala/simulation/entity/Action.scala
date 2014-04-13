package se.ramn.bottfarmen.simulation.entity


trait Action {
  def bot: Bot
}

case class Move(bot: Bot, position: Position) extends Action
