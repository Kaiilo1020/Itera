package itera.infrastructure.persistence

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import itera.infrastructure.config.DatabaseConfig

object DatabaseTransactor {
  def make[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](config.poolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = ce
      )
    } yield xa
  }
}
