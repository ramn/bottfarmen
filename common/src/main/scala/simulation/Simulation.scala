package se.ramn.bottfarmen.simulation

import collection.immutable.Iterable
import se.ramn.bottfarmen.api.BotCommander
import impl.SimulationImpl


trait Simulation {
  def doTurn: Unit
  def botCommanders: Iterable[BotCommanderView]
  def bots: Iterable[BotView]
}


object Simulation {
  def apply(
    commanders: Set[BotCommander],
    scenario: Scenario
  ): Simulation = {
    new SimulationImpl(commanders, scenario)
  }
}
