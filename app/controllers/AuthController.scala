package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{BaseController, ControllerComponents, Action}
import play.api.libs.json.{Json, JsError, OFormat}
import scala.concurrent.{ExecutionContext, Future}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import itera.features.auth.commands.{AuthHandlers, RegisterCommand, LoginCommand, AuthResponse}

/**
 * AuthController refactored to use Dependency Injection.
 * All infrastructure is now injected via the constructor, making it 100% testable.
 */
@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  handlers: AuthHandlers[IO] // Injected handler
)(implicit ec: ExecutionContext) extends BaseController {

  // JSON formatters
  implicit val authResponseFormat: OFormat[AuthResponse] = Json.format[AuthResponse]
  implicit val registerCommandFormat: OFormat[RegisterCommand] = Json.format[RegisterCommand]
  implicit val loginCommandFormat: OFormat[LoginCommand] = Json.format[LoginCommand]

  def register() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterCommand].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      cmd => handlers.handleRegister(cmd).unsafeToFuture().map {
        case Right(resp) => Created(Json.toJson(resp))
        case Left(err) => BadRequest(Json.obj("message" -> err.message))
      }
    )
  }

  def login() = Action.async(parse.json) { implicit request =>
    request.body.validate[LoginCommand].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      cmd => handlers.handleLogin(cmd).unsafeToFuture().map {
        case Right(resp) => Ok(Json.toJson(resp))
        case Left(err) => Unauthorized(Json.obj("message" -> err.message))
      }
    )
  }
}
