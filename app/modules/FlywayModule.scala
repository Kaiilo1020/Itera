package modules

import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import org.flywaydb.core.Flyway
import play.api.db.Database
import play.api.{Configuration, Logging}

/**
 * Guice module that binds the FlywayMigrator as an eager singleton.
 * This ensures migrations run immediately when the application starts.
 */
class FlywayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FlywayMigrator]).asEagerSingleton()
  }
}

/**
 * Component responsible for executing Flyway migrations.
 * It reuses the application's default connection pool (HikariCP).
 */
@Singleton
class FlywayMigrator @Inject()(
    db: Database,
    configuration: Configuration
) extends Logging {

  private val locations = configuration.getOptional[Seq[String]]("flyway.locations")
    .getOrElse(Seq("db/migration"))

  logger.info(s"🚀 [Flyway] Initializing migrations using default database pool")
  logger.info(s"📂 [Flyway] Searching for migrations in: ${locations.mkString(", ")}")

  try {
    val flyway = Flyway.configure()
      .dataSource(db.dataSource)
      .locations(locations: _*)
      .baselineOnMigrate(true)
      .connectRetries(5) // Increased for cloud stability
      .load()

    val result = flyway.migrate()
    
    if (result.success) {
      if (result.migrationsExecuted > 0) {
        logger.info(s"✅ [Flyway] Successfully applied ${result.migrationsExecuted} migrations.")
      } else {
        logger.info("✅ [Flyway] Database is already up to date.")
      }
    } else {
      logger.error("❌ [Flyway] Migration reported failure.")
    }
  } catch {
    case e: Exception =>
      val msg = e.getMessage
      if (msg.contains("permission denied for schema public")) {
        logger.error("❌ [Flyway] PERMISSION ERROR: Your database user does not have permission to create the flyway_schema_history table in 'public'.")
        logger.error("💡 TIP: Run 'GRANT ALL ON SCHEMA public TO your_user;' in your Neon console or use a more privileged role (e.g. neondb_owner).")
      } else {
        logger.error(s"❌ [Flyway] CRITICAL ERROR during migration: $msg")
      }
      throw e
  }
}
