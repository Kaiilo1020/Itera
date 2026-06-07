package itera.features.auth.presentation

import cats.effect.Async
import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import itera.features.auth.commands.{AuthHandlers, LoginCommand, RegisterCommand}
import itera.shared.domain.DomainError

class AuthRoutes[F[_]: Async](handlers: AuthHandlers[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "auth" / "register" =>
      for {
        cmd <- req.as[RegisterCommand]
        result <- handlers.handleRegister(cmd)
        resp <- result match {
          case Right(authResp) => Created(authResp)
          case Left(error) => BadRequest(error)
        }
      } yield resp

    case req @ POST -> Root / "auth" / "login" =>
      for {
        cmd <- req.as[LoginCommand]
        result <- handlers.handleLogin(cmd)
        resp <- result match {
          case Right(authResp) => Ok(authResp)
          case Left(error) => Unauthorized(error)
        }
      } yield resp
  }
}
