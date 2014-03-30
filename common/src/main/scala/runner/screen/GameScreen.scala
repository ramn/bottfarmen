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
import se.ramn.bottfarmen.engine.{TileMap, BotCommanderLoader, BotCommanderArbiter, Scenario}
import se.ramn.bottfarmen.util.Times


class GameScreen(val game: BottfarmenGuiRunner) extends ScreenWithVoidImpl {
  val tilesize = 16
  private val commanderArbiter = buildCommanderArbiter
  private val camera = buildCamera
  private val shapeRenderer = new ShapeRenderer
  private val turnIntervalSecs = 1f
  private val terrainTexture = new Texture(Gdx.files.internal("assets/data/terrainsprites.png"))
  private val objectTexture = new Texture(Gdx.files.internal("assets/data/objectsprites.png"))
  private val terrainSprites = Map(
    '.' -> new TextureRegion(terrainTexture, 0, 0, tilesize, tilesize),
    '~' -> new TextureRegion(terrainTexture, 1 * tilesize, 0, tilesize, tilesize)
  )
  private val objectSprites = Map(
    "p1" -> new TextureRegion(objectTexture, 0, 0, tilesize, tilesize),
    "p2" -> new TextureRegion(objectTexture, 1 * tilesize, 0, tilesize, tilesize)
  )
  private val map = TileMap.loadFromFile("assets/data/testmap.txt")
  private object propertiesHud {
    val leftOffset = 1050
    val leftBorderOffset = leftOffset - 50
    val propertiesOffset = game.height - 40
    val propertiesBoxHeight = 100
    def propsOffsets = Iterator.iterate(propertiesOffset) { x =>
      x - propertiesBoxHeight
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
      commanderArbiter.doTurn
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
    game.batch.begin()
    drawTerrain()
    drawBots()
    game.batch.end()
    drawPropertiesHudFrame()
    game.batch.begin()
    drawBotProperties()
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
      (x, y) = tileCoord(rowIx, colIx)
    } game.batch.draw(terrainSprites(cell), x, y)
  }

  private def drawBots() = {
    commanderArbiter.bots foreach { bot =>
      val (x, y) = bot.position
      val commanderString =  s"p${bot.commanderId+1}"
      game.batch.draw(objectSprites(commanderString), x, y)
    }
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
    shapeRenderer.line(
      leftBorderOffset, game.height,
      leftBorderOffset, 0)
    shapeRenderer.end()
  }

  private def drawBotProperties() = {
    import propertiesHud._
    val offsets = propsOffsets
    commanderArbiter.bots foreach { bot =>
      val (x, y) = (leftOffset, offsets.next)
      val text =  s"C${bot.commanderId}B${bot.id}"
      game.batch.setColor(Color.WHITE)
      game.font.draw(game.batch, text, x, y)
    }
  }

  private def tileCoord(row: Int, col: Int): (Int, Int) = (col * tilesize, game.height-(row * tilesize)-tilesize)

  private def buildCommanderArbiter = {
    val commanders = BotCommanderLoader.loadFromEnv
    val scenario = new Scenario {
      val mapRows = game.height / tilesize
      val mapCols = game.width / tilesize
      val startingPositions = List(
        (50, 50),
        (100, 100),
        (150, 150),
        (200, 200))
    }
    BotCommanderArbiter(commanders, scenario)
  }

  private def buildCamera = {
    val camera = new OrthographicCamera()
    camera.setToOrtho(false, game.width, game.height)
    camera
  }
}
