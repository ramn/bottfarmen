package se.ramn.bottfarmen

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import se.ramn.bottfarmen.runner.BottfarmenGuiRunner


object DesktopStarter extends App {
    val cfg = new LwjglApplicationConfiguration()
    cfg.title = "BottfarmenGuiRunner"
    cfg.width = 1280
    cfg.height = 800
    cfg.vSyncEnabled = true
    new LwjglApplication(new BottfarmenGuiRunner(width=cfg.width, height=cfg.height), cfg)
}
