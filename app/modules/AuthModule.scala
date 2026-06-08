package modules

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import play.api.db.Database
import play.api.Configuration
import doobie.util.transactor.Transactor
import cats.effect.IO
import itera.features.auth.domain.AuthRepository
import itera.features.auth.infrastructure.DoobieAuthRepository
import itera.features.auth.commands.AuthHandlers
import itera.shared.infrastructure.{JwtTokenService, TokenService}
import java.util.Properties

/**
 * Guice module to provide dependencies for the Authentication slice.
 */
class AuthModule extends AbstractModule {
  override def configure(): Unit = {
    // We use @Provides methods for more complex instantiation
  }

  @Provides
  @Singleton
  def provideTransactor(config: Configuration): Transactor[IO] = {
    val dbUrl = config.get[String]("db.default.url")
    val dbUser = config.get[String]("db.default.username")
    val dbPass = config.get[String]("db.default.password")
    
    val props = new Properties()
    props.setProperty("user", dbUser)
    props.setProperty("password", dbPass)
    
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", dbUrl, props, None
    )
  }

  @Provides
  @Singleton
  def provideAuthRepository(xa: Transactor[IO]): AuthRepository[IO] = {
    new DoobieAuthRepository[IO](xa)
  }

  @Provides
  @Singleton
  def provideTokenService(config: Configuration): TokenService[IO] = {
    val jwtSecret = config.get[String]("jwt.secret")
    new JwtTokenService[IO](jwtSecret, 86400)
  }

  @Provides
  @Singleton
  def provideAuthHandlers(repo: AuthRepository[IO], tokenService: TokenService[IO]): AuthHandlers[IO] = {
    new AuthHandlers[IO](repo, tokenService)
  }
}
