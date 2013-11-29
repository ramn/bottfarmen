import sbt._

import Keys._

object Settings {
  lazy val common = Defaults.defaultSettings ++ Seq (
    version := "0.1",
    scalaVersion := "2.10.1",
    updateLibgdxTask
   )

  lazy val desktop = Settings.common ++ Seq (
    fork in Compile := true
  )

  //lazy val android = Settings.common ++
    //AndroidProject.androidSettings ++
    //AndroidMarketPublish.settings ++ Seq (
      //platformName in Android := "android-17",
      //keyalias in Android := "change-me",
      //mainAssetsPath in Android := file("common/src/main/resources"),
      //unmanagedBase <<= baseDirectory( _ /"src/main/libs" ),
      //proguardOption in Android := "-keep class com.badlogic.gdx.backends.android.** { *; }"
    //)

  val updateLibgdx = TaskKey[Unit]("update-gdx", "Updates libgdx")

  val updateLibgdxTask = updateLibgdx <<= streams map { (s: TaskStreams) =>
    import Process._
    import java.io._
    import java.net.URL
    import java.nio.file.Paths
    import java.nio.file.Files
    
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

    s.log.info("Extracting android libs")
    val androidDest = file("android/src/main/libs")
    val androidFilter = new ExactFilter("gdx-backend-android.jar") |
    new ExactFilter("armeabi/libgdx.so") |
    new ExactFilter("armeabi/libandroidgl20.so") |
    new ExactFilter("armeabi-v7a/libgdx.so") |
    new ExactFilter("armeabi-v7a/libandroidgl20.so")
    IO.unzip(zipFile, androidDest, androidFilter)

    // Destroy the file.
    zipFile.delete
    s.log.info("Update complete")
  }
}

object LibgdxBuild extends Build {
  val common = Project (
    "common",
    file("common"),
    settings = Settings.common
  )

  lazy val desktop = Project (
    "desktop",
    file("desktop"),
    settings = Settings.desktop
  ) dependsOn common

  //lazy val android = Project (
    //"android",
    //file("android"),
    //settings = Settings.android
  //) dependsOn common
}
