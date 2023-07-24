// Equivalent to gradle's allprojects, this defines a setting across every project in the build
ThisBuild / scalaVersion := "3.3.0"

// In SBT before we can define a custom task, we have to define a "task key"
// These will be used in the ScalaJS client for compiling the Scala to JavaScript
val deployFast = taskKey[Unit]("Copies the fastLinkJS script to deployscripts/")
val deployFull = taskKey[Unit]("Copies the fullLinkJS script to deployscripts/")

// SBT's way of defining a project in a subdirectory looks like code
lazy val scalajsClient = project.in(file("scala-js-client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "mgnScalaJS",

    resolvers += "jitpack" at "https://jitpack.io",

    libraryDependencies ++= Seq(
      "com.wbillingsley" %%% "doctacular" % "0.3.0",

      "com.github.wbillingsley.handy" %%% "handy" % "v0.11-SNAPSHOT", // For Latch[T]

      "org.scalameta" %%% "munit" % "0.7.29" % Test
    ), 

    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    // Used by GitHub Actions to get the script out from the .gitignored target directory
    deployFast := {
      val opt = (Compile / fastOptJS).value
      IO.copyFile(opt.data, new java.io.File("target/compiled.js"))
    },

    deployFull := {
      val opt = (Compile / fullOptJS).value
      IO.copyFile(opt.data, new java.io.File("target/compiled.js"))
    }

  )

// The ScalaFX project is probably a better fit for most teams that like Scala
lazy val scalafxClient = project.in(file("scalafx-client"))
  .settings(
    name := "mgnScalaFX",

    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "20.0.0-R31", 

      "org.scalameta" %%% "munit" % "0.7.29" % Test
    ), 

    Compile / run / fork := true
  )







