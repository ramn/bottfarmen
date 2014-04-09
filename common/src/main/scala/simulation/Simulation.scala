package se.ramn.bottfarmen.simulation

import collection.immutable.Iterable
import se.ramn.bottfarmen.api
import impl.SimulationImpl
import entity.BotCommander


trait Simulation {
  def doTurn: Unit
  def botCommanders: Iterable[BotCommanderView]
  def bots: Iterable[BotView]
}


object Simulation {
  def apply(
    playerCommanders: Set[api.BotCommander],
    scenario: Scenario
  ): Simulation = {
    val commanders = playerCommanders
      .zipWithIndex
      .map((BotCommander.apply _).tupled)
    new SimulationImpl(commanders, scenario)
  }
}
