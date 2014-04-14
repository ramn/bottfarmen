package se.ramn.bottfarmen.runner.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera

import se.ramn.bottfarmen.runner.BottfarmenGuiRunner
import se.ramn.bottfarmen.simulation.Simulation


class GameOverScreen(val game: BottfarmenGuiRunner, simulation: Simulation) extends ScreenWithVoidImpl {

  val camera = new OrthographicCamera
  camera.setToOrtho(false, game.width, game.height)

  def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(0, 0, 0.2f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    camera.update()
    game.batch.setProjectionMatrix(camera.combined)

    game.batch.begin()
    val leftOffset = 100
    val textLineOffsets = textLineOffsetsIter
    def drawtextln(text: String) = {
      val (x, y) = (leftOffset, textLineOffsets.next)
      game.font.draw(game.batch, text, x, y)
    }
    drawtextln("Game Over!")
    textLineOffsets.next
    if (simulation.victor.isDefined) {
      drawtextln(s"Victor: ${simulation.victor.get}")
    } else {
      drawtextln("Everybody lost.")
    }
    drawtextln("Click anywhere to quit")
    game.batch.end()

    if (Gdx.input.isTouched) {
      dispose()
      System.exit(0)
    }
  }

  def textLineOffsetsIter = Iterator.iterate(game.height - 100) { x =>
    val lineHeight = 36
    x - lineHeight
  }
}
