package se.ramn.bottfarmen.runner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

import se.ramn.bottfarmen.runner.screen.GameScreen


class BottfarmenGuiRunner(val width: Int, val height: Int) extends Game {
  private var myFont: BitmapFont = _
  private var myBatch: SpriteBatch = _

  def font = myFont
  def batch = myBatch

  override def create() {
    myFont = buildFont
    myBatch = new SpriteBatch
    setScreen(new GameScreen(this))
  }

  override def render() {
    super.render()
  }

  override def dispose() {
    batch.dispose()
    font.dispose()
  }

  private def buildFont = {
    import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
    import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
    def fontConf(size: Int) = {
      val fontConfig = new FreeTypeFontParameter()
      fontConfig.size = size
      fontConfig
    }
    val fontFile = Gdx.files.internal("assets/font/Courier_Prime.ttf")
    val generator = new FreeTypeFontGenerator(fontFile)
    val fontSmall: BitmapFont = generator.generateFont(fontConf(size=16))
    //val fontLarge: BitmapFont = generator.generateFont(fontConf(size=22))
    generator.dispose()
    fontSmall
  }
}
