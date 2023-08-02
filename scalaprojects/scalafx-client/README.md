# ScalaFX client

This client uses ScalaFX as its UI framework, which might be familiar to COSC250 students.
MUnit is used as a unit testing framework.

Most other dependencies are kept as similar as possible to the Java projects. e.g.

* Vertx is used for networking
* Jackson (via Vertx) is used for JSON serialisation and deserialisation

Note that as we don't have access to the "common" suproject from the Java/Gradle projects, 
some classes from that project are re-implemented directly in this project. Particularly,
the rendering and commands classes.