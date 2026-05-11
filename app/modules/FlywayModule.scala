package modules

import com.google.inject.AbstractModule
import org.flywaydb.core.Flyway
import play.api.{Configuration, Environment, Logging}

import javax.inject.Inject

class FlywayModule(environment: Environment, configuration: Configuration) extends AbstractModule with Logging {
  override def configure(): Unit = {
    val url = configuration.get[String]("db.default.url")
    val user = configuration.get[String]("db.default.username")
    val password = configuration.get[String]("db.default.password")
    val locations = configuration.getOptional[String]("flyway.locations").getOrElse("db/migration")

    logger.info(s"Running Flyway migrations on $url")

    val flyway = Flyway.configure()
      .dataSource(url, user, password)
      .locations(locations)
      .baselineOnMigrate(true)
      .load()

    flyway.migrate()
    logger.info("Flyway migrations completed")
  }
}
