package itera.infrastructure.config

import cats.effect.Sync
import cats.syntax.all._
import pureconfig.{ConfigSource, ConfigReader}
import pureconfig.generic.semiauto._

object ConfigLoader {

  implicit val dbConfigReader: ConfigReader[DatabaseConfig] = deriveReader[DatabaseConfig]
  implicit val jwtConfigReader: ConfigReader[JwtConfig]     = deriveReader[JwtConfig]
  implicit val serverConfigReader: ConfigReader[ServerConfig] = deriveReader[ServerConfig]
  implicit val appConfigReader: ConfigReader[AppConfig]      = deriveReader[AppConfig]

  def load[F[_]: Sync]: F[AppConfig] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])
}
