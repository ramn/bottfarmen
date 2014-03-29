package se.ramn.bottfarmen.runner.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera

import se.ramn.bottfarmen.runner.BottfarmenGuiRunner


class MainMenuScreen(val game: BottfarmenGuiRunner) extends ScreenWithVoidImpl {

  val camera = new OrthographicCamera
  camera.setToOrtho(false, game.width, game.height)

  def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(0, 0, 0.2f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    camera.update()
    game.batch.setProjectionMatrix(camera.combined)

    game.batch.begin()
    game.font.draw(game.batch, "Welcome to Bottfarmen! ", 100, 150)
    game.font.draw(game.batch, "Click anywhere to begin", 100, 100)
    game.batch.end()

    if (Gdx.input.isTouched) {
      game.setScreen(new GameScreen(game))
      dispose()
    }
  }
}
