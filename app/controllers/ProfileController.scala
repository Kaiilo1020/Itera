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
import itera.features.profile.commands.{InitializeProfileCommand, ProfileHandlers, UpdateProfileCommand}
import itera.features.profile.queries.{ProfileQueryHandlers, StudentProfileView}
import itera.features.profile.domain.Skill
import itera.features.profile.infrastructure.DoobieStudentRepository
import itera.features.goals.infrastructure.DoobieGoalRepository
import itera.features.progress.infrastructure.DoobieProgressRepository
import itera.shared.infrastructure.JwtTokenService
import itera.shared.infrastructure.LogicClient
import itera.shared.infrastructure.IAClient
import doobie.util.transactor.Transactor
import play.api.Configuration
import java.util.UUID
import java.util.Properties

@Singleton
class ProfileController @Inject()(
  val controllerComponents: ControllerComponents,
  config: Configuration,
  logicClient: LogicClient,
  iaClient: IAClient
)(implicit ec: ExecutionContext) extends BaseController {

  // JSON formatters
  implicit val skillFormat: OFormat[Skill] = Json.format[Skill]
  implicit val studentProfileViewFormat: OFormat[StudentProfileView] = Json.format[StudentProfileView]
  implicit val initializeProfileCommandFormat: OFormat[InitializeProfileCommand] = Json.format[InitializeProfileCommand]
  implicit val updateProfileCommandFormat: OFormat[UpdateProfileCommand] = Json.format[UpdateProfileCommand]

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
  
  private val studentRepo = new DoobieStudentRepository[IO](xa)
  private val goalRepo = new DoobieGoalRepository[IO](xa)
  private val progressRepo = new DoobieProgressRepository[IO](xa)
  
  private val tokenService = new JwtTokenService[IO](jwtSecret, 86400)
  private val handlers = new ProfileHandlers[IO](studentRepo)
  private val queryHandlers = new ProfileQueryHandlers[IO](studentRepo, goalRepo, progressRepo, logicClient, iaClient)

  // Middleware helper to extract userId from JWT
  private def withAuth[A](action: UUID => Future[play.api.mvc.Result])(implicit request: play.api.mvc.Request[A]): Future[play.api.mvc.Result] = {
    val maybeToken = request.cookies.get("itera_auth").map(_.value) orElse 
                    request.headers.get("Authorization").filter(_.startsWith("Bearer ")).map(_.substring(7))

    maybeToken match {
      case Some(token) =>
        tokenService.validate(token).unsafeToFuture().flatMap {
          case Right((userId, _)) => action(userId)
          case Left(_) => Future.successful(Unauthorized(Json.obj("message" -> "Invalid token")))
        }
      case _ => Future.successful(Unauthorized(Json.obj("message" -> "Missing or invalid session")))
    }
  }

  def getProfile() = Action.async { implicit request =>
    withAuth { userId =>
      queryHandlers.getProfile(userId).unsafeToFuture().map {
        case Right(profile) => Ok(Json.toJson(profile))
        case Left(err) => NotFound(Json.obj("message" -> err.message))
      }
    }
  }

  def initialize() = Action.async(parse.json) { implicit request =>
    withAuth { userId =>
      request.body.validate[InitializeProfileCommand].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        cmd => {
          if (cmd.userId != userId) {
            Future.successful(Forbidden(Json.obj("message" -> "User ID mismatch")))
          } else {
            handlers.handleInitialize(cmd).unsafeToFuture().map {
              case Right(_) => Created
              case Left(err) => BadRequest(Json.obj("message" -> err.message))
            }
          }
        }
      )
    }
  }

  def update() = Action.async(parse.json) { implicit request =>
    withAuth { userId =>
      request.body.validate[UpdateProfileCommand].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        cmd => {
          if (cmd.userId != userId) {
            Future.successful(Forbidden(Json.obj("message" -> "User ID mismatch")))
          } else {
            handlers.handleUpdate(cmd).unsafeToFuture().map {
              case Right(_) => Ok
              case Left(err) => BadRequest(Json.obj("message" -> err.message))
            }
          }
        }
      )
    }
  }
}
