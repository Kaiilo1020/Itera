ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "itera"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "itera",

    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-Wconf:cat=unused:info",
      "-feature",
      "-deprecation"
    ),

    libraryDependencies ++= Seq(
      guice,
      ws,
      jdbc,

      // Cats Effect
      "org.typelevel" %% "cats-effect" % "3.5.4",

      // Doobie
      "org.tpolecat" %% "doobie-core"           % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari"         % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres"       % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres-circe" % "1.0.0-RC5",

      // Circe
      "io.circe" %% "circe-core"    % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser"  % "0.14.9",

      // JWT
      "com.github.jwt-scala" %% "jwt-circe" % "10.0.1",

      // BCrypt
      "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",

      // PostgreSQL
      "org.postgresql" % "postgresql" % "42.7.3",

      // Flyway
      "org.flywaydb" % "flyway-core" % "10.15.0",
      "org.flywaydb" % "flyway-database-postgresql" % "10.15.0",

      // Testing
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.mockito" % "mockito-core" % "5.12.0" % Test,
      "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % Test,
      "com.h2database" % "h2" % "2.2.224" % Test
    ),

    dependencyOverrides ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-core" % "2.14.3",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.14.3",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.3"
    )
  )
