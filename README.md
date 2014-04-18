Install
============

    sbt updateGdx


Running game
============

Start with two example bot commanders:

    COMMANDERS="se.ramn.bottfarmen.example.baseraider.BaseRaiderCommander,se.ramn.bottfarmen.example.idle.IdleCommander" ./run

To set a custom map, provide the path to the map file in the environment:

		MAP_FILE="assets/data/testmap.txt"


Build standalone jar
====================
A standalone jar, including every dependency and even scala itself, can be
built thus:

		sbt assembly

It can be run with only java present. Note, it won't include the assets directory!


Run standalone jar
==================
We cd into desktop/ project, since the assets dir lives there and it needs to
be present, since it is not included in the jar.

		cd desktop/
		export MAP_FILE="assets/data/testmap.dev.txt"
    export COMMANDERS="se.ramn.bottfarmen.example.baseraider.BaseRaiderCommander,se.ramn.bottfarmen.example.idle.IdleCommander"
		java -jar desktop/target/scala-2.10/desktop-assembly-0.1.jar
