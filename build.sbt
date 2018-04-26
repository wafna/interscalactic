name := "interscalactic"
organization in ThisBuild := "org.wafna"
scalaVersion in ThisBuild := "2.12.4"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    common,
    server
  )

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= commonDependencies
  )

lazy val akkaHttpVersion = "10.1.0"
lazy val akkaVersion = "2.5.11"
lazy val server = project
  .settings(
    name := "server",
    settings,
    assemblySettings,
    mainClass in (Compile, run) := Some("myPackage.aMainClass"),
    libraryDependencies ++= commonDependencies ++ Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "ch.megard" %% "akka-http-cors" % "0.3.0",
      "com.typesafe.slick" %% "slick" % "3.2.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.3.175",
      "org.apache.tika" % "tika-core" % "1.17",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
    ),
    mainClass in Compile := Some("demo.Usage")
  )
  .dependsOn(common)

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val settings = commonSettings ++ wartremoverSettings ++ scalafmtSettings

lazy val dependencies =
  new {
    val scalatestV = "3.0.4"
    val scalacheckV = "1.13.5"
    val scalatest = "org.scalatest" %% "scalatest" % scalatestV
    val scalacheck = "org.scalacheck" %% "scalacheck" % scalacheckV
  }

lazy val commonDependencies = Seq(dependencies.scalatest % "test", dependencies.scalacheck % "test")

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    // "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val wartremoverSettings = Seq(wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.NonUnitStatements, Wart.Var))

lazy val scalafmtSettings =
  Seq(scalafmtOnCompile := true, scalafmtTestOnCompile := true, scalafmtVersion := "1.2.0")

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)
