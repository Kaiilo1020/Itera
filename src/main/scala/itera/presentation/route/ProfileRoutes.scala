package itera.presentation.route

import cats.effect.Concurrent
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import itera.application.command.UpdateProfileCommand
import itera.application.command.handler.UpdateProfileHandler
import itera.application.query.GetProfileQuery
import itera.application.query.handler.GetProfileHandler
import itera.domain.error.DomainError
import itera.domain.valueobject.EntityId
import itera.presentation.dto.{ErrorResponse, UpdateProfileRequest}

class ProfileRoutes[F[_]: Concurrent](
  getHandler: GetProfileHandler[F],
  updateHandler: UpdateProfileHandler[F]
) extends Http4sDsl[F] {

  implicit val updateProfileReqDecoder: org.http4s.EntityDecoder[F, UpdateProfileRequest] = jsonOf[F, UpdateProfileRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "profiles" / userId =>
      EntityId.fromString(userId) match {
        case Left(err) => BadRequest(ErrorResponse("invalid_id", err).asJson)
        case Right(id) =>
          getHandler.handle(GetProfileQuery(id)).flatMap {
            case Left(err) => domainErrorToResponse(err)
            case Right(dto) => Ok(dto.asJson)
          }
      }

    case req @ PUT -> Root / "profiles" / userId =>
      EntityId.fromString(userId) match {
        case Left(err) => BadRequest(ErrorResponse("invalid_id", err).asJson)
        case Right(id) =>
          req.as[UpdateProfileRequest].flatMap { request =>
            request.toDomain(id) match {
              case Left(err) => BadRequest(ErrorResponse("validation_error", err).asJson)
              case Right((name, interests, edu)) =>
                val cmd = UpdateProfileCommand(id, name, interests, edu)
                updateHandler.handle(cmd).flatMap {
                  case Left(err) => domainErrorToResponse(err)
                  case Right(_) => NoContent()
                }
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
