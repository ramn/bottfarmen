package se.ramn.bottfarmen.runner.screen

import com.badlogic.gdx.Screen


trait ScreenWithVoidImpl extends Screen {
  override def resize(width: Int, height: Int) = ()
  override def show = ()
  override def hide() = ()
  override def pause() = ()
  override def resume() = ()
  override def dispose() = ()
}
