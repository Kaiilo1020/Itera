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
import itera.features.goals.commands.{GoalHandlers, SetGoalsCommand}
import itera.features.goals.domain.{Goal, GoalRepository}
import itera.features.goals.infrastructure.DoobieGoalRepository
import itera.features.profile.infrastructure.DoobieStudentRepository
import itera.shared.infrastructure.JwtTokenService
import doobie.util.transactor.Transactor
import play.api.Configuration
import java.util.UUID
import play.api.libs.json.Writes._
import play.api.libs.json.Reads._
import java.util.Properties

@Singleton
class GoalController @Inject()(
  val controllerComponents: ControllerComponents,
  config: Configuration
)(implicit ec: ExecutionContext) extends BaseController {

  // JSON formatters
  implicit val goalFormat: OFormat[Goal] = Json.format[Goal]
  implicit val setGoalsCommandFormat: OFormat[SetGoalsCommand] = Json.format[SetGoalsCommand]

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
  
  private val goalRepo = new DoobieGoalRepository[IO](xa)
  private val studentRepo = new DoobieStudentRepository[IO](xa)
  private val tokenService = new JwtTokenService[IO](jwtSecret, 86400)
  private val handlers = new GoalHandlers[IO](goalRepo, studentRepo)

  private def withAuth[A](action: UUID => Future[play.api.mvc.Result])(implicit request: play.api.mvc.Request[A]): Future[play.api.mvc.Result] = {
    request.headers.get("Authorization") match {
      case Some(auth) if auth.startsWith("Bearer ") =>
        val token = auth.substring(7)
        tokenService.validate(token).unsafeToFuture().flatMap {
          case Right((userId, _)) => action(userId)
          case Left(_) => Future.successful(Unauthorized(Json.obj("message" -> "Invalid token")))
        }
      case _ => Future.successful(Unauthorized(Json.obj("message" -> "Missing or invalid Authorization header")))
    }
  }

  def getGoals() = Action.async { implicit request =>
    withAuth { userId =>
      (for {
        student <- studentRepo.findByUserId(userId)
        goal    <- student.map(s => goalRepo.findByStudentId(s.id)).getOrElse(IO.pure(None))
      } yield goal).unsafeToFuture().map {
        case Some(goal) => Ok(Json.toJson(goal))
        case None => NotFound(Json.obj("message" -> "Goals not found"))
      }
    }
  }

  def setGoals() = Action.async(parse.json) { implicit request =>
    withAuth { userId =>
      request.body.validate[SetGoalsCommand].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        cmd => {
          if (cmd.userId != userId) {
            Future.successful(Forbidden(Json.obj("message" -> "User ID mismatch")))
          } else {
            handlers.handleSetGoals(cmd).unsafeToFuture().map {
              case Right(_) => Ok
              case Left(err) => BadRequest(Json.obj("message" -> err.message))
            }
          }
        }
      )
    }
  }
}
