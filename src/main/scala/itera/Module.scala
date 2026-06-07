package itera

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import itera.infrastructure.config.{AppConfig, ConfigLoader}
import itera.infrastructure.persistence.{DatabaseTransactor, Migrations}
import itera.features.auth.infrastructure.DoobieAuthRepository
import itera.features.auth.infrastructure.AuthMiddlewareBuilder
import itera.features.auth.commands.AuthHandlers
import itera.features.auth.presentation.AuthRoutes
import itera.shared.infrastructure.JwtTokenService
import itera.shared.infrastructure.TokenService
import itera.presentation.HealthRoutes
import org.http4s.server.AuthMiddleware
import java.util.UUID

object Module {

  def resources[F[_]: Async]: Resource[F, AppResources[F]] = {
    for {
      config <- Resource.eval(ConfigLoader.load[F])
      xa     <- DatabaseTransactor.make[F](config.database)
      _      <- Resource.eval(Migrations.run[F](xa))
    } yield AppResources(config, xa)
  }

  def build[F[_]: Async](xa: HikariTransactor[F], config: AppConfig): App[F] = {
    // Shared Infrastructure
    val tokenService: TokenService[F] = new JwtTokenService[F](config.jwt.secret, config.jwt.ttlSeconds)
    val authMiddleware: AuthMiddleware[F, (UUID, String)] = AuthMiddlewareBuilder[F](tokenService)

    // Auth Feature Slice
    val authRepo     = new DoobieAuthRepository[F](xa)
    val authHandlers = new AuthHandlers[F](authRepo, tokenService)
    val authRoutes   = new AuthRoutes[F](authHandlers)

    // Shared Routes
    val healthRoutes = new HealthRoutes[F]

    App(
      authRoutes     = authRoutes,
      healthRoutes   = healthRoutes,
      authMiddleware = authMiddleware,
      tokenService   = tokenService,
      config         = config
    )
  }

  final case class AppResources[F[_]](config: AppConfig, xa: HikariTransactor[F])

  final case class App[F[_]](
    authRoutes: AuthRoutes[F],
    healthRoutes: HealthRoutes[F],
    authMiddleware: AuthMiddleware[F, (UUID, String)],
    tokenService: TokenService[F],
    config: AppConfig
  )
}
