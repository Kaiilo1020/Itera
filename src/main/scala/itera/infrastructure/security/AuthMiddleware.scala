package itera.infrastructure.security

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.all._
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import itera.domain.valueobject.EntityId

object IteraAuthMiddleware {

  def apply[F[_]: Sync](tokenService: TokenService[F]): AuthMiddleware[F, (EntityId, String)] = {
    val authUser = Kleisli[({type L[A] = OptionT[F, A]})#L, Request[F], (EntityId, String)] { request =>
      OptionT.fromOption[F](request.headers.get[org.http4s.headers.Authorization]).flatMap {
        case org.http4s.headers.Authorization(org.http4s.Credentials.Token(_, token)) =>
          OptionT.liftF(tokenService.validate(token)).subflatMap {
            case Right(result) => Some(result)
            case Left(_) => None
          }
        case _ =>
          OptionT.none[F, (EntityId, String)]
      }
    }
    AuthMiddleware(authUser)
  }
}
