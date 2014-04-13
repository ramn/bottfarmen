import org.scalatest.FunSuite

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.MoveResolver
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.TileMap
import se.ramn.bottfarmen.simulation.Scenario


class MoveResolverTest extends FunSuite {
  test("resolve") {

    val scenario = new Scenario {
      override val tilemap = TileMap.loadFromFile("common/src/test/assets/testmap.txt")
    }

    val commander1 = new BotCommander {
      val id = 1
      val name = "C1"
      var bots = Set.empty[Bot]
      def requestCommands(gameState: api.GameState) = ???
      var homeBase: Base = new Base(800, 99, 99)
    }

    val commander2 = new BotCommander {
      val id = 2
      val name = "C2"
      var bots = Set.empty[Bot]
      def requestCommands(gameState: api.GameState) = ???
      var homeBase: Base = new Base(800, 101, 101)
    }



    val bot1 = new Bot(1, commander1) {
      var row = 1
      var col = 1
      var hitpoints = 100
    }
    commander1.bots += bot1

    val bot2 = new Bot(2, commander2) {
      var row = 2
      var col = 2
      var hitpoints = 100
    }
    commander2.bots += bot2

    val bot3 = new Bot(3, commander1) {
      var row = 2
      var col = 1
      var hitpoints = 100
    }
    commander1.bots += bot3


    val bot4 = new Bot(4, commander1) {
      var row = 1
      var col = 8
      var hitpoints = 100
    }
    commander1.bots += bot4




    val target = new MoveResolver(
      Map(
        bot1 -> Position(row=1, col=2),
        bot2 -> Position(row=1, col=2),
        bot3 -> Position(row=1, col=1),
        bot4 -> Position(row=1, col=9)),
      Set.empty,
      scenario)

    val unhandledMovers = target.resolve()

    assert(bot1.row === 1)
    assert(bot1.col === 1)

    assert(bot2.row === 2)
    assert(bot2.col === 2)

    assert(bot3.row === 2)
    assert(bot3.col === 1)

    assert(bot4.row === 1)
    assert(bot4.col === 9)

    assert(bot1.hitpoints === 60)
    assert(bot2.hitpoints === 60)
    assert(bot3.hitpoints === 100)

    assert(unhandledMovers.size === 1)
    assert(unhandledMovers.keys.head === bot3)
  }
}
