import sbt._

import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._


object Settings {
  lazy val common = Project.defaultSettings ++ Seq(
    version := "0.1",
    scalaVersion := "2.10.4",
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-deprecation",
      "-encoding", "utf8"
    ),
    javacOptions ++= Seq("-Xlint:unchecked"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.1.3" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.1.RC1" % "test"
    )
  )

  lazy val desktop = Settings.common ++ Seq (
    fork in Compile := true
  ) ++ assemblySettings

  lazy val updateGdx = taskKey[Unit]("Downloads libgdx")

  lazy val updateGdxSetting = updateGdx := {
    import Process._
    import java.io._
    import java.net.URL
    import java.nio.file.Paths
    import java.nio.file.Files
    
    val s: TaskStreams = streams.value

    // Declare names
    val baseUrl = "http://libgdx.badlogicgames.com/nightlies"
    val gdxName = "libgdx-nightly-latest"

    // Fetch the file.
    s.log.info("Pulling %s" format(gdxName))
    s.log.warn("This may take a few minutes...")
    val zipName = "%s.zip" format(gdxName)
    val zipFile = new java.io.File(zipName)
    val url = new URL("%s/%s" format(baseUrl, zipName))
    IO.download(url, zipFile)

    // Extract jars into their respective lib folders.
    s.log.info("Extracting common libs")
    val commonDest = file("common/lib")
    val commonFilter = new ExactFilter("gdx.jar") |
      new ExactFilter("extensions/gdx-freetype/gdx-freetype.jar")
    IO.unzip(zipFile, commonDest, commonFilter)
    try {
      Files.move(
        Paths.get("common/lib/extensions/gdx-freetype/gdx-freetype.jar"),
        Paths.get("common/lib/gdx-freetype.jar")
      )
      Files.delete(Paths.get("common/lib/extensions/gdx-freetype"))
      Files.delete(Paths.get("common/lib/extensions"))
    } catch {
      case e: Exception => println(e)
    }


    s.log.info("Extracting desktop libs")
    val freetypePath = "extensions/gdx-freetype"
    val freetypeJar = "gdx-freetype-natives.jar"
    val desktopDest = file("desktop/lib")
    val desktopFilter = new ExactFilter("gdx-natives.jar") |
    new ExactFilter("gdx-backend-lwjgl.jar") |
    new ExactFilter("gdx-backend-lwjgl-natives.jar") |
    new ExactFilter(Paths.get(freetypePath, freetypeJar).toString)
    IO.unzip(zipFile, desktopDest, desktopFilter)
    try {
      Files.move(
        Paths.get(desktopDest.toString, freetypePath, freetypeJar),
        Paths.get(desktopDest.toString, freetypeJar)
      )
      Files.delete(Paths.get(desktopDest.toString, freetypePath))
      Files.delete(Paths.get(desktopDest.toString, "extensions"))
    } catch {
      case e: Exception => println(e)
    }

    // Destroy the file.
    zipFile.delete
    s.log.info("Update complete")
  }
}


object LibgdxBuild extends Build {
  val common = Project(
    "bottfarmen-common",
    file("common"),
    settings = Settings.common :+ Settings.updateGdxSetting
  )

  lazy val desktop = Project (
    "bottfarmen-desktop",
    file("desktop"),
    settings = Settings.desktop
  ) dependsOn common
}
