package itera.infrastructure.security

import cats.effect.Sync
import cats.syntax.all._
import java.time.Instant
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import io.circe.parser.parse
import io.circe.syntax._
import itera.domain.valueobject.EntityId

class JwtTokenService[F[_]: Sync](secret: String, ttlSeconds: Long) extends TokenService[F] {

  private val algorithm = JwtAlgorithm.HS256

  def issue(userId: EntityId, role: String): F[String] =
    Sync[F].delay {
      val now = Instant.now().getEpochSecond
      val claim = JwtClaim(
        content = Map("userId" -> userId.show, "role" -> role).asJson.noSpaces
      )
        .issuedAt(now)
        .expiresAt(now + ttlSeconds)
      Jwt.encode(claim, secret, algorithm)
    }

  def validate(token: String): F[Either[String, (EntityId, String)]] =
    Sync[F].delay {
      Jwt.decode(token, secret, Seq(algorithm)).toEither.leftMap(_.getMessage).flatMap { claim =>
        parse(claim.content).leftMap(_.getMessage).flatMap { json =>
          for {
            userId <- json.hcursor.get[String]("userId").leftMap(_.getMessage)
            role   <- json.hcursor.get[String]("role").leftMap(_.getMessage)
            id     <- EntityId.fromString(userId)
          } yield (id, role)
        }
      }
    }
}
