import org.scalatest.FunSuite
import org.scalatest.OneInstancePerTest

import collection.immutable.Seq

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.SimulationImpl
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.Action
import se.ramn.bottfarmen.simulation.entity.Move
import se.ramn.bottfarmen.simulation.entity.Attack
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.TileMap
import se.ramn.bottfarmen.simulation.Scenario


class SimulationTest extends FunSuite with OneInstancePerTest {
  val target = new SimulationImpl(null, null)

  val commander1 = new BotCommander {
    val id = 1
    val name = "C1"
    var bots = Set.empty[Bot]
    def requestCommands(gameState: api.GameState) = ???
    var homeBase: Base = new Base(800, 99, 99)
  }

  val bot1 = new Bot(1, commander1) {
    var row = 1
    var col = 1
    var hitpoints = 100
  }

  val bot2 = new Bot(2, commander1) {
    var row = 3
    var col = 3
    var hitpoints = 100
  }

  test("filter max one command per bot") {
    val actions1: Seq[Action] = Seq(Move(bot1, Position(1, 1)))
    assert(target.filterMaxOneCommandPerBot(actions1) === actions1)

    val actions2: Seq[Action] = Seq(
      Move(bot1, Position(1, 1)),
      Move(bot1, Position(2, 2))
    )
    assert(target.filterMaxOneCommandPerBot(actions2) === Seq(actions2(0)))

    val actions3 = Seq(
      Move(bot1, Position(1, 1)),
      Move(bot2, Position(3, 4)),
      Move(bot1, Position(2, 2))
    )
    assert(target.filterMaxOneCommandPerBot(actions3) === actions3.take(2))

    val actions4 = Seq(
      Attack(bot1, Position(1, 1)),
      Move(bot1, Position(2, 2)),
      Move(bot2, Position(3, 4)),
      Attack(bot2, Position(3, 4)))
    assert(target.filterMaxOneCommandPerBot(actions4) === Seq(actions4(0), actions4(2)))
  }
}
