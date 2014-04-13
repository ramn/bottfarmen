package se.ramn.bottfarmen.simulation

import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Position


trait Action

case class Move(bot: Bot, position: Position) extends Action
