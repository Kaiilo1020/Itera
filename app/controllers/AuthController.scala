package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents
import play.api.mvc.Action
import play.api.libs.json.Json
import play.api.libs.json.JsError
import play.api.libs.json.OFormat
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import itera.features.auth.commands.AuthHandlers
import itera.features.auth.commands.RegisterCommand
import itera.features.auth.commands.LoginCommand
import itera.features.auth.commands.AuthResponse
import itera.features.auth.infrastructure.DoobieAuthRepository
import itera.shared.infrastructure.JwtTokenService
import doobie.util.transactor.Transactor
import play.api.Configuration
import java.util.Properties

@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  config: Configuration
)(implicit ec: ExecutionContext) extends BaseController {

  // JSON formatters
  implicit val authResponseFormat: OFormat[AuthResponse] = Json.format[AuthResponse]
  implicit val registerCommandFormat: OFormat[RegisterCommand] = Json.format[RegisterCommand]
  implicit val loginCommandFormat: OFormat[LoginCommand] = Json.format[LoginCommand]

  // Infrastructure setup
  private val dbUrl = config.get[String]("db.default.url")
  private val dbUser = config.get[String]("db.default.username")
  private val dbPass = config.get[String]("db.default.password")
  private val jwtSecret = config.get[String]("jwt.secret")
  
  private val props = new Properties()
  props.setProperty("user", dbUser)
  props.setProperty("password", dbPass)
  
  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", dbUrl, props, None
  )
  
  private val authRepo = new DoobieAuthRepository[IO](xa)
  private val tokenService = new JwtTokenService[IO](jwtSecret, 86400)
  private val handlers = new AuthHandlers[IO](authRepo, tokenService)

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
