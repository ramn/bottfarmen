Install
============

Requirements: java sdk 7, sbt

    sbt updateGdx


Running game
============

Start with two example bot commanders (must be on class path):

    COMMANDERS="se.ramn.bottfarmen.example.baseraider.BaseRaiderCommander,se.ramn.bottfarmen.example.idle.IdleCommander" ./run

To set a custom map, provide the path to the map file in the environment. The
path should be absolute, where the root is the resources/ directory. The path
should therefore start with a slash.
Example:

    MAP_FILE="/assets/data/testmap.txt"

To change log level (default is Info), set the environment variable:

    LOG_LEVEL=Debug

Available logging levels: Trace, Debug, Info, Warning, Error, Fatal


Standalone jar
==============

Build standalone jar
--------------------
A standalone jar, including every dependency and even scala itself, can be
built thus:

    sbt assembly

It can be run with only java present.


Run standalone jar
------------------

    #export LOG_LEVEL=Debug # optional
    #export MAP_FILE="/assets/data/testmap.txt" # opional
    export COMMANDERS="se.ramn.bottfarmen.example.baseraider.BaseRaiderCommander,se.ramn.bottfarmen.example.idle.IdleCommander"
    java -cp desktop/target/scala-2.10/bottfarmen-desktop-assembly-0.1.jar se.ramn.bottfarmen.DesktopStarter


Run your own bot
================

Compile your own bot to java class files or preferrably to a jar, in a separate
project. Run the simulation by following the instructions under "Standalone
jar", but make sure to add your bot to the java class path.

Example:

    export COMMANDERS="my.bot.Commander,se.ramn.bottfarmen.example.idle.IdleCommander"
    java -cp path/to/bottfarmen-desktop-assembly.jar:path/to/my/bot.jar se.ramn.bottfarmen.DesktopStarter

If you are building your bot in Scala, using SBT, then you can build a jar and
publish it to your local ivy cache (~/.ivy2/) by running `sbt publishLocal`, or
build a jar which will be placed in the `target/scala..` directory by running
`sbt package`.
