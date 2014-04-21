package se.ramn.bottfarmen.runner.screen

import collection.JavaConversions._

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.graphics.g2d.{TextureRegion, Sprite}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

import se.ramn.bottfarmen.runner.BottfarmenGuiRunner
import se.ramn.bottfarmen.simulation.TileMap
import se.ramn.bottfarmen.simulation.BotCommanderLoader
import se.ramn.bottfarmen.simulation.Simulation
import se.ramn.bottfarmen.simulation.Scenario
import se.ramn.bottfarmen.simulation.Tiles
import se.ramn.bottfarmen.simulation.view.BotCommanderView
import se.ramn.bottfarmen.util.Times


class GameScreen(val game: BottfarmenGuiRunner) extends ScreenWithVoidImpl {
  val tilesize = 16
  private lazy val simulation: Simulation = buildSimulation
  private lazy val camera = buildCamera
  private val shapeRenderer = new ShapeRenderer
  private val turnIntervalSecs = 0.5f
  private lazy val terrainTexture =
    new Texture(Gdx.files.internal("assets/data/terrainsprites.png"))
  private lazy val objectTexture =
    new Texture(Gdx.files.internal("assets/data/objectsprites.png"))
  private lazy val terrainSprites = buildTerrainSprites
  private lazy val objectSprites = buildObjectSprites
  private lazy val map = TileMap.fromEnvOrDefault(
    defaultMapPath="/assets/data/testmap.txt")
  private object propertiesHud {
    val hudLeftMargin = tilesize * 2
    val leftOffset = tilesize * 64 + hudLeftMargin
    val leftBorderOffset = leftOffset - hudLeftMargin
    val propertiesOffset = game.height - 30
    val lineHeight = 18
    def hudLineOffsetsIter = Iterator.iterate(propertiesOffset) { x =>
      x - lineHeight
    }
  }

  private var gameTimeSecs = 0f

  override def render(delta: Float) {
    gameTimeSecs += delta // must be done first
    update(delta)
    draw(delta)
  }

  private def update(delta: Float) = {
    camera.update() // update camera matrices
    processInput(delta)
    turnsToPerform(delta).timesDo {
      if (simulation.isGameOver) {
        game.setScreen(new GameOverScreen(game, simulation))
      } else {
        simulation.doTurn
      }
    }
  }

  private def processInput(delta: Float) = {
    // process user input
    if (Gdx.input.isTouched) {
      val touchPos = new Vector3
      touchPos.set(Gdx.input.getX, Gdx.input.getY, 0)
      camera.unproject(touchPos)
      // do something with touchPos
    }

    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      // do something on left key press
    }
  }

  private def turnsToPerform(delta: Float): Int = {
    val previousGameTime = gameTimeSecs - delta
    var turnsToPerform = 0
    var deltaLeft = delta
    while (deltaLeft > 0) {
      val remaining = deltaLeft - turnIntervalSecs
      if (remaining > 0) {
        turnsToPerform += 1
      } else {
        val passedIntervalBoundaryByAFraction =
          ((gameTimeSecs / turnIntervalSecs).floor >
            (previousGameTime / turnIntervalSecs).floor)
        if (passedIntervalBoundaryByAFraction) {
          turnsToPerform += 1
        }
      }
      deltaLeft = remaining
    }
    turnsToPerform
  }

  private def draw(delta: Float) = {
    clearScreen()

    // tell the SpriteBatch to render in the coordinate system specified by the
    // camera.
    game.batch.setProjectionMatrix(camera.combined)

    // draw between batch.begin() and batch.end()
    inBatch {
      drawTerrain()
      drawBots()
    }
    drawPropertiesHudFrame()
    inBatch {
      drawBotProperties()
    }
  }

  private def inBatch(thunk: => Unit) = {
    game.batch.begin()
    thunk
    game.batch.end()
  }

  private def clearScreen() = {
    // clear the screen with a dark blue color. The arguments to glClearColor
    // are the red, green blue and alpha component in the range [0,1] of the
    // color to be used to clear the screen.
    Gdx.gl.glClearColor(0, 0, 0.2f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
  }

  private def drawTerrain() = {
    for {
      (row, rowIx) <- map.rows.zipWithIndex
      (cell, colIx) <- row.zipWithIndex
      (x, y) = tileCoord(row=rowIx, col=colIx)
    } game.batch.draw(terrainSprites(cell), x, y)
    for {
      pos <- simulation.spawnedFood
      (x, y) = tileCoord(row=pos.row, col=pos.col)
    } game.batch.draw(terrainSprites(Tiles.Food), x, y)
  }

  private def drawBots() = {
    simulation.bots foreach { bot =>
      val (x, y) = tileCoord(row=bot.row, col=bot.col)
      game.batch.draw(botSprite(bot.commanderId), x, y)
    }
  }

  private def botSprite(commanderId: Int) = {
    val commanderString = s"p${commanderId+1}"
    objectSprites(commanderString)
  }

  private def drawPropertiesHudFrame() = {
    import propertiesHud._
    shapeRenderer.setProjectionMatrix(camera.combined)
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    shapeRenderer.setColor(Color.BLACK)
    shapeRenderer.rect(
      leftBorderOffset, 0,
      game.width - leftBorderOffset, game.height)
    shapeRenderer.end()

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
    shapeRenderer.setColor(Color.WHITE)
    val lineLeftOffset = leftBorderOffset + 1
    shapeRenderer.line(
      lineLeftOffset, game.height,
      lineLeftOffset, 0)
    shapeRenderer.end()
  }

  private def drawBotProperties() = {
    import propertiesHud._
    val offsets = hudLineOffsetsIter
    def drawtextln(text: String) = {
      val (x, y) = (leftOffset, offsets.next)
      game.font.draw(game.batch, text, x, y)
    }
    def drawSprite(commander: BotCommanderView) = {
      val (x, y) = (leftOffset, offsets.next)
      game.batch.draw(botSprite(commander.id), x, y)
    }
    game.batch.setColor(Color.WHITE)
    drawtextln(s"Turn no: ${simulation.turnNo}")
    offsets.next
    offsets.next
    simulation.botCommanders foreach { commander =>
      drawSprite(commander)
      drawtextln(commander.name)
      drawtextln(s"Commander id: ${commander.id}")
      drawtextln(s"Home base hp: ${commander.homeBase.hitpoints}")
      commander.bots foreach { bot =>
        drawtextln(s"Bot id: ${bot.id}")
        drawtextln(s"Bot hitpoints: ${bot.hitpoints}")
      }
      offsets.next
      offsets.next
      offsets.next
    }
  }

  private def tileCoord(row: Int, col: Int): (Int, Int) =
    (col * tilesize, game.height-(row * tilesize)-tilesize)

  private def buildSimulation = {
    val self = this
    val commanders = BotCommanderLoader.loadFromEnv
    val scenario = new Scenario {
      override val tilemap = self.map
      override val maxFoodTilesCount =
        5 + ((tilemap.rowCount * tilemap.colCount) / 300)
      override val maxBotCountPerCommander =
        5 + ((tilemap.rowCount * tilemap.colCount) / 400)
    }
    Simulation(commanders, scenario)
  }

  private def buildCamera = {
    val camera = new OrthographicCamera()
    camera.setToOrtho(false, game.width, game.height)
    camera
  }

  private def buildTerrainSprites = {
    def selectTexture(row: Int, col: Int) = {
      new TextureRegion(
        terrainTexture,
        col * tilesize,
        row * tilesize,
        tilesize,
        tilesize)
    }
    val walkable = selectTexture(row=0, col=0)
    val water    = selectTexture(row=0, col=1)
    val baseBot0 = selectTexture(row=0, col=2)
    val baseBot1 = selectTexture(row=0, col=3)
    val food     = selectTexture(row=0, col=4)
    Map(
      Tiles.Land -> walkable,
      Tiles.Water -> water,
      Tiles.Food -> food,
      Tiles.HomeCommander0 -> baseBot0,
      Tiles.HomeCommander1 -> baseBot1
    ).withDefaultValue(walkable)
  }

  private def buildObjectSprites = Map(
    "p1" -> new TextureRegion(objectTexture, 0, 0, tilesize, tilesize),
    "p2" -> new TextureRegion(objectTexture, 1 * tilesize, 0, tilesize, tilesize)
  )
}
