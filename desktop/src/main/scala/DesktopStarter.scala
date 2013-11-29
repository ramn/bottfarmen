package se.ramn

import com.badlogic.gdx.backends.lwjgl._

object DesktopStarter extends App {
    val cfg = new LwjglApplicationConfiguration()
    cfg.title = "MyGame"
    cfg.width = 800
    cfg.height = 800
    cfg.useGL20 = true
    new LwjglApplication(new MyGame(width=cfg.width, height=cfg.height), cfg)
}
