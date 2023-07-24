name := "mgnScala"

ThisBuild / scalaVersion := "3.3.0"

val deployFast = taskKey[Unit]("Copies the fastLinkJS script to deployscripts/")
val deployFull = taskKey[Unit]("Copies the fullLinkJS script to deployscripts/")


lazy val scalajsClient = project.in(file("scala-js-client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
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





