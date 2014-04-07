Install
============

    sbt updateGdx


Running game
============

    ./run

Start with one dummy BotCommander

    COMMANDERS="se.ramn.bottfarmen.example.DummyBotCommander" ./run

Start with two dummy BotCommanders

    COMMANDERS="se.ramn.bottfarmen.example.DummyBotCommander,se.ramn.bottfarmen.example.DummyBotCommander" ./run


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
		export COMMANDERS="se.ramn.bottfarmen.example.DummyBotCommander,se.ramn.bottfarmen.example.DummyBotCommander"
		java -jar desktop/target/scala-2.10/desktop-assembly-0.1.jar
