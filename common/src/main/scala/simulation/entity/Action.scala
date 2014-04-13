package se.ramn.bottfarmen.simulation.entity


sealed trait Action {
  def bot: Bot
}

case class Move(bot: Bot, position: Position) extends Action

case class Attack(bot: Bot, position: Position) extends Action
