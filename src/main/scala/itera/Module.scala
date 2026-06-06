package itera

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import itera.application.command.handler.{AdvanceProgressHandler, CreateUserHandler, UpdateProfileHandler}
import itera.application.query.handler.{GetProfileHandler, GetProgressHandler, GetUserByIdHandler}
import itera.application.service.PasswordHasher
import itera.infrastructure.config.{AppConfig, ConfigLoader}
import itera.infrastructure.persistence.{DatabaseTransactor, Migrations, ProfileRepositoryImpl, ProgressRepositoryImpl, UserRepositoryImpl}
import itera.infrastructure.security.{IteraAuthMiddleware, JwtTokenService, PasswordHasherImpl}
import itera.presentation.route.{HealthRoutes, ProfileRoutes, ProgressRoutes, UserRoutes}

object Module {

  def resources[F[_]: Async]: Resource[F, AppResources[F]] = {
    for {
      config <- Resource.eval(ConfigLoader.load[F])
      xa     <- DatabaseTransactor.make[F](config.database)
      _      <- Resource.eval(Migrations.run[F](xa))
    } yield AppResources(config, xa)
  }

  def build[F[_]: Async](xa: HikariTransactor[F], config: AppConfig): App[F] = {
    // Repositories
    val userRepo     = new UserRepositoryImpl[F](xa)
    val profileRepo  = new ProfileRepositoryImpl[F](xa)
    val progressRepo = new ProgressRepositoryImpl[F](xa)

    // Services
    val hasher       = new PasswordHasherImpl[F]
    val tokenService = new JwtTokenService[F](config.jwt.secret, config.jwt.ttlSeconds)

    // Command Handlers
    val createUserHandler     = new CreateUserHandler[F](userRepo, progressRepo, hasher)
    val updateProfileHandler  = new UpdateProfileHandler[F](profileRepo)
    val advanceProgressHandler = new AdvanceProgressHandler[F](progressRepo)

    // Query Handlers
    val getUserByIdHandler  = new GetUserByIdHandler[F](userRepo)
    val getProfileHandler   = new GetProfileHandler[F](profileRepo)
    val getProgressHandler  = new GetProgressHandler[F](progressRepo)

    // Routes
    val userRoutes     = new UserRoutes[F](createUserHandler, getUserByIdHandler)
    val profileRoutes  = new ProfileRoutes[F](getProfileHandler, updateProfileHandler)
    val progressRoutes = new ProgressRoutes[F](getProgressHandler, advanceProgressHandler)
    val healthRoutes   = new HealthRoutes[F]

    // Auth middleware
    val authMiddleware = IteraAuthMiddleware[F](tokenService)

    App(
      userRoutes     = userRoutes,
      profileRoutes  = profileRoutes,
      progressRoutes = progressRoutes,
      healthRoutes   = healthRoutes,
      authMiddleware = authMiddleware,
      tokenService   = tokenService,
      config         = config
    )
  }

  final case class AppResources[F[_]](config: AppConfig, xa: HikariTransactor[F])

  final case class App[F[_]](
    userRoutes: UserRoutes[F],
    profileRoutes: ProfileRoutes[F],
    progressRoutes: ProgressRoutes[F],
    healthRoutes: HealthRoutes[F],
    authMiddleware: org.http4s.server.AuthMiddleware[F, (itera.domain.valueobject.EntityId, String)],
    tokenService: itera.infrastructure.security.TokenService[F],
    config: AppConfig
  )
}
