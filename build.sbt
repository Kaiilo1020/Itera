ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "itera"

lazy val root = (project in file("."))
  .settings(
    name := "Itera",
    Compile / mainClass := Some("IteraApp"),
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-Wconf:cat=unused:info",
      "-feature",
      "-deprecation"
    ),
    libraryDependencies ++= Seq(
      // Cats Effect
      "org.typelevel" %% "cats-effect" % "3.5.4",

      // Http4s
      "org.http4s" %% "http4s-ember-server" % "0.23.27",
      "org.http4s" %% "http4s-ember-client" % "0.23.27",
      "org.http4s" %% "http4s-circe"        % "0.23.27",
      "org.http4s" %% "http4s-dsl"          % "0.23.27",

      // Doobie
      "org.tpolecat" %% "doobie-core"     % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari"   % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-h2"       % "1.0.0-RC5",

      // Circe
      "io.circe" %% "circe-core"    % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser"  % "0.14.9",

      // JWT
      "com.github.jwt-scala" %% "jwt-circe" % "10.0.1",

      // PureConfig
      "com.github.pureconfig" %% "pureconfig-core"    % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-generic" % "0.17.7",

      // BCrypt
      "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",

      // PostgreSQL
      "org.postgresql" % "postgresql" % "42.7.3",

      // Flyway
      "org.flywaydb" % "flyway-core" % "10.15.0",
      "org.flywaydb" % "flyway-database-postgresql" % "10.15.0",

      // H2 (tests)
      "com.h2database" % "h2" % "2.2.224",

      // Logback
      "ch.qos.logback" % "logback-classic" % "1.5.6"
    )
  )
