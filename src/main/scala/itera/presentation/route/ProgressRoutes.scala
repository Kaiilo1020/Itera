package itera.presentation.route

import cats.effect.Concurrent
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import itera.application.command.AdvanceProgressCommand
import itera.application.command.handler.AdvanceProgressHandler
import itera.application.query.GetProgressQuery
import itera.application.query.handler.GetProgressHandler
import itera.domain.error.DomainError
import itera.domain.valueobject.EntityId
import itera.presentation.dto.{AdvanceProgressRequest, ErrorResponse}

class ProgressRoutes[F[_]: Concurrent](
  getHandler: GetProgressHandler[F],
  advanceHandler: AdvanceProgressHandler[F]
) extends Http4sDsl[F] {

  implicit val advanceReqDecoder: org.http4s.EntityDecoder[F, AdvanceProgressRequest] = jsonOf[F, AdvanceProgressRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "progress" / userId =>
      EntityId.fromString(userId) match {
        case Left(err) => BadRequest(ErrorResponse("invalid_id", err).asJson)
        case Right(id) =>
          getHandler.handle(GetProgressQuery(id)).flatMap {
            case Left(err) => domainErrorToResponse(err)
            case Right(list) => Ok(list.asJson)
          }
      }

    case req @ POST -> Root / "progress" / userId =>
      EntityId.fromString(userId) match {
        case Left(err) => BadRequest(ErrorResponse("invalid_id", err).asJson)
        case Right(id) =>
          req.as[AdvanceProgressRequest].flatMap { request =>
            val cmd = AdvanceProgressCommand(id, request.act, request.stage)
            advanceHandler.handle(cmd).flatMap {
              case Left(err) => domainErrorToResponse(err)
              case Right(_) => NoContent()
            }
          }
      }
  }

  private def domainErrorToResponse(err: DomainError): F[Response[F]] = err match {
    case DomainError.NotFound(_, _)    => NotFound(ErrorResponse("not_found", err.message).asJson)
    case DomainError.ValidationError(_)  => BadRequest(ErrorResponse("validation_error", err.message).asJson)
    case _                             => InternalServerError(ErrorResponse("internal_error", err.message).asJson)
  }
}
