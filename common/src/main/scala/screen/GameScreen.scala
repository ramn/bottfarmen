package se.ramn.screen

import java.util.Iterator
import collection.JavaConversions._

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer

import se.ramn.MyGame


class GameScreen(val game: MyGame) extends Screen {
  // create the camera and the SpriteBatch
  val camera = new OrthographicCamera()
  camera.setToOrtho(false, game.width, game.height)

  override def render(delta: Float) {
    // clear the screen with a dark blue color. The arguments to glClearColor
    // are the red, green blue and alpha component in the range [0,1] of the
    // color to be used to clear the screen.
    Gdx.gl.glClearColor(0, 0, 0.2f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // tell the camera to update its matrices.
    camera.update()

    // tell the SpriteBatch to render in the coordinate system specified by the
    // camera.
    game.batch.setProjectionMatrix(camera.combined)

    // draw between batch.begin() and batch.end()
    game.batch.begin()
    game.font.draw(game.batch, "Hello World!", 0, game.height)
    //game.batch.draw(someSprite, x, y)
    game.batch.end()


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

  override def resize(width: Int, height: Int) {
  }

  override def show {
  }

  override def hide() {
  }

  override def pause() {
  }

  override def resume() {
  }

  override def dispose() {
  }
}
