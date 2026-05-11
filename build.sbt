ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "itera"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "Itera",
    libraryDependencies ++= Seq(
      guice,
      ws,
      jdbc,
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.flywaydb" % "flyway-core" % "10.14.0",
      "org.flywaydb" % "flyway-database-postgresql" % "10.14.0",
      "io.jsonwebtoken" % "jjwt-api" % "0.12.6",
      "io.jsonwebtoken" % "jjwt-impl" % "0.12.6" % Runtime,
      "io.jsonwebtoken" % "jjwt-jackson" % "0.12.6" % Runtime,
      "org.mindrot" % "jbcrypt" % "0.4",
      specs2 % Test
    )
  )
