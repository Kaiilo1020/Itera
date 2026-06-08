package itera.shared.infrastructure

import cats.effect.Sync
import cats.syntax.all._
import java.time.Instant
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import io.circe.parser.parse
import io.circe.syntax._
import java.util.UUID

class JwtTokenService[F[_]: Sync](secret: String, ttlSeconds: Long) extends TokenService[F] {

  private val algorithm = JwtAlgorithm.HS256

  override def issue(userId: UUID, role: String): F[String] =
    Sync[F].delay {
      val now = Instant.now().getEpochSecond
      val claim = JwtClaim(
        content = Map("userId" -> userId.toString, "role" -> role).asJson.noSpaces
      )
        .issuedAt(now)
        .expiresAt(now + ttlSeconds)
      Jwt.encode(claim, secret, algorithm)
    }

  override def validate(token: String): F[Either[String, (UUID, String)]] =
    Sync[F].delay {
      Jwt.decode(token, secret, Seq(algorithm)).toEither.leftMap(_.getMessage).flatMap { claim =>
        parse(claim.content).leftMap(_.getMessage).flatMap { json =>
          for {
            userIdStr <- json.hcursor.get[String]("userId").leftMap(_.getMessage)
            role      <- json.hcursor.get[String]("role").leftMap(_.getMessage)
            userId    <- Either.catchNonFatal(UUID.fromString(userIdStr)).leftMap(_.getMessage)
          } yield (userId, role)
        }
      }
    }
}
