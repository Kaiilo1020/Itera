package itera.presentation.route

import cats.effect.Concurrent
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import itera.application.command.CreateUserCommand
import itera.application.command.handler.CreateUserHandler
import itera.application.query.GetUserByIdQuery
import itera.application.query.handler.GetUserByIdHandler
import itera.domain.error.DomainError
import itera.domain.valueobject.EntityId
import itera.presentation.dto.{CreateUserRequest, ErrorResponse}

class UserRoutes[F[_]: Concurrent](
  createHandler: CreateUserHandler[F],
  getHandler: GetUserByIdHandler[F]
) extends Http4sDsl[F] {

  implicit val createUserReqDecoder: org.http4s.EntityDecoder[F, CreateUserRequest] = jsonOf[F, CreateUserRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      req.as[CreateUserRequest].flatMap { request =>
        request.toDomain match {
          case Left(err) =>
            BadRequest(ErrorResponse("validation_error", err).asJson)
          case Right((email, role)) =>
            val cmd = CreateUserCommand(email, request.password, role)
            createHandler.handle(cmd).flatMap {
              case Left(err) => domainErrorToResponse(err)
              case Right(id) => Created(Map("id" -> id.show).asJson)
            }
        }
      }

    case GET -> Root / "users" / id =>
      EntityId.fromString(id) match {
        case Left(err) =>
          BadRequest(ErrorResponse("invalid_id", err).asJson)
        case Right(entityId) =>
          getHandler.handle(GetUserByIdQuery(entityId)).flatMap {
            case Left(err) => domainErrorToResponse(err)
            case Right(dto) => Ok(dto.asJson)
          }
      }
  }

  private def domainErrorToResponse(err: DomainError): F[Response[F]] = err match {
    case DomainError.NotFound(_, _)    => NotFound(ErrorResponse("not_found", err.message).asJson)
    case DomainError.AlreadyExists(_, _) => Conflict(ErrorResponse("already_exists", err.message).asJson)
    case DomainError.ValidationError(_)  => BadRequest(ErrorResponse("validation_error", err.message).asJson)
    case _                             => InternalServerError(ErrorResponse("internal_error", err.message).asJson)
  }
}
