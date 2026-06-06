package itera.infrastructure.config

final case class DatabaseConfig(
  driver: String,
  url: String,
  user: String,
  password: String,
  poolSize: Int
)

final case class JwtConfig(
  secret: String,
  ttlSeconds: Long
)

final case class ServerConfig(
  host: String,
  port: Int
)

final case class AppConfig(
  database: DatabaseConfig,
  jwt: JwtConfig,
  server: ServerConfig
)
