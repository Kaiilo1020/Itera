package itera.features.auth.infrastructure

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.all._
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import itera.shared.infrastructure.TokenService
import java.util.UUID

object AuthMiddlewareBuilder {

  def apply[F[_]: Sync](tokenService: TokenService[F]): AuthMiddleware[F, (UUID, String)] = {
    val authUser = Kleisli[({type L[A] = OptionT[F, A]})#L, Request[F], (UUID, String)] { request =>
      OptionT.fromOption[F](request.headers.get[org.http4s.headers.Authorization]).flatMap { authHeader =>
        authHeader.credentials match {
          case org.http4s.Credentials.Token(_, token) =>
            OptionT.liftF(tokenService.validate(token)).subflatMap {
              case Right(result) => Some(result)
              case Left(_) => None
            }
          case _ =>
            OptionT.none[F, (UUID, String)]
        }
      }
    }
    AuthMiddleware(authUser)
  }
}
