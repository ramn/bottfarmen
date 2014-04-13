import org.scalatest.FunSuite
import org.scalatest.OneInstancePerTest

import se.ramn.bottfarmen.api
import se.ramn.bottfarmen.simulation.MoveResolver
import se.ramn.bottfarmen.simulation.entity.Bot
import se.ramn.bottfarmen.simulation.entity.Base
import se.ramn.bottfarmen.simulation.entity.BotCommander
import se.ramn.bottfarmen.simulation.entity.Position
import se.ramn.bottfarmen.simulation.TileMap
import se.ramn.bottfarmen.simulation.Scenario


class MoveResolverTest extends FunSuite with OneInstancePerTest {
  val scenario = new Scenario {
    override val tilemap = TileMap.loadFromFile("common/src/test/assets/testmap.txt")
  }
  val commander1 = createCommander(1)
  val commander2 = createCommander(2)

  def createCommander(id: Int) = {
    val myId = id
    new BotCommander {
      val id = myId
      val name = s"C$myId"
      var bots = Set.empty[Bot]
      def requestCommands(gameState: api.GameState) = ???
      var homeBase: Base = new Base(800, 10 * myId, 10 * myId)
    }
  }

  def createBot(id: Int, commander: BotCommander, row: Int, col: Int): Bot = {
    val (myRow, myCol) = (row, col)
    val bot = new Bot(id, commander) {
      var row = myRow
      var col = myCol
      var hitpoints = 100
    }
    commander.bots += bot
    bot
  }

  test("enemy bots wants the same tile, they stay put but deal damage") {
    val bot1 = createBot(1, commander1, row=1, col=1)
    val bot2 = createBot(2, commander2, row=2, col=2)
    val bot3 = createBot(3, commander1, row=2, col=1)
    val bot4 = createBot(4, commander1, row=1, col=8)

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

  test("chain of bots bumping into each other, noone moves, no friendly fire") {
    val bot1 = createBot(1, commander1, row=1, col=0)
    val bot2 = createBot(2, commander1, row=2, col=0)
    val bot3 = createBot(3, commander1, row=3, col=0)
    val bot4 = createBot(4, commander1, row=4, col=0)

    val target = new MoveResolver(
      Map(
        bot2 -> Position(row=1, col=0),
        bot3 -> Position(row=2, col=0),
        bot4 -> Position(row=3, col=0)
        ),
      Set(bot1),
      scenario)
    val unhandledMovers = target.resolve()

    assert(bot1.hitpoints === 100)
    assert(bot2.hitpoints === 100)
    assert(bot3.hitpoints === 100)
    assert(bot4.hitpoints === 100)

    assert(bot1.row === 1)
    assert(bot2.row === 2)
    assert(bot3.row === 3)
    assert(bot4.row === 4)
    assert(
      Seq(bot1, bot2, bot3, bot4).map(_.col).forall(_ == 0),
      "All bots should still be in column 0")
  }

  test("chain of bots should move forward like ducks in a row") {
    val bot1 = createBot(1, commander1, row=1, col=0)
    val bot2 = createBot(2, commander1, row=2, col=0)
    val bot3 = createBot(3, commander1, row=3, col=0)
    val bot4 = createBot(4, commander1, row=4, col=0)

    val target = new MoveResolver(
      Map(
        bot1 -> Position(row=0, col=0),
        bot2 -> Position(row=1, col=0),
        bot3 -> Position(row=2, col=0),
        bot4 -> Position(row=3, col=0)
        ),
      Set.empty,
      scenario)
    val unhandledMovers = target.resolve()

    assert(bot1.hitpoints === 100)
    assert(bot2.hitpoints === 100)
    assert(bot3.hitpoints === 100)
    assert(bot4.hitpoints === 100)

    assert(bot1.row === 0)
    assert(bot2.row === 1)
    assert(bot3.row === 2)
    assert(bot4.row === 3)

    assert(
      Seq(bot1, bot2, bot3, bot4).map(_.col).forall(_ == 0),
      "All bots should still be in column 0")
  }
}
