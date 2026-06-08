package modules

import com.google.inject.AbstractModule
import org.flywaydb.core.Flyway
import play.api.{Configuration, Environment, Logging}

class FlywayModule(environment: Environment, configuration: Configuration) extends AbstractModule with Logging {
  override def configure(): Unit = {
    val url = configuration.get[String]("db.default.url")
    val user = configuration.get[String]("db.default.username")
    val password = configuration.get[String]("db.default.password")
    
    // Explicitly set location to search in 'conf' (which is root of classpath)
    val locations = configuration.getOptional[Seq[String]]("flyway.locations")
      .getOrElse(Seq("db/migration"))

    logger.info(s"🚀 [Flyway] Attempting to run migrations on: $url")
    logger.info(s"📂 [Flyway] Searching for migrations in: ${locations.mkString(", ")}")

    try {
      val flyway = Flyway.configure()
        .dataSource(url, user, password)
        .locations(locations: _*)
        .baselineOnMigrate(true)
        .connectRetries(3) // Robustness for cloud connections
        .load()

      val result = flyway.migrate()
      if (result.success) {
        logger.info(s"✅ [Flyway] Successfully applied ${result.migrationsExecuted} migrations.")
      } else {
        logger.warn(s"⚠️ [Flyway] Migration finished but success flag is false.")
      }
    } catch {
      case e: Exception =>
        logger.error(s"❌ [Flyway] CRITICAL ERROR during migration initialization: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
