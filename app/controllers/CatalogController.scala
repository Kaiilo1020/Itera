package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents
import play.api.mvc.Action
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import play.api.libs.json.JsError
import scala.concurrent.ExecutionContext
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import itera.features.admin.domain.{CatalogRepository, Institution, ProgressState}
import itera.features.admin.infrastructure.DoobieCatalogRepository
import doobie.util.transactor.Transactor
import play.api.Configuration
import java.util.UUID
import java.util.Properties

@Singleton
class CatalogController @Inject()(
  val controllerComponents: ControllerComponents,
  config: Configuration
)(implicit ec: ExecutionContext) extends BaseController {

  // JSON formatters
  implicit val institutionFormat: OFormat[Institution] = Json.format[Institution]
  implicit val progressStateFormat: OFormat[ProgressState] = Json.format[ProgressState]

  // Infrastructure setup
  private val dbUrl = config.get[String]("db.default.url")
  private val dbUser = config.get[String]("db.default.username")
  private val dbPass = config.get[String]("db.default.password")
  
  private val props = new Properties()
  props.setProperty("user", dbUser)
  props.setProperty("password", dbPass)

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", dbUrl, props, None
  )
  
  private val catalogRepo = new DoobieCatalogRepository[IO](xa)

  def listInstitutions() = Action.async { implicit request =>
    catalogRepo.listInstitutions().unsafeToFuture().map { institutions =>
      Ok(Json.toJson(institutions))
    }
  }

  def createInstitution() = Action.async(parse.json) { implicit request =>
    request.body.validate[Institution].fold(
      errors => IO.pure(BadRequest(JsError.toJson(errors))),
      institution => catalogRepo.saveInstitution(institution).as(Created)
    ).unsafeToFuture()
  }

  def updateInstitution(id: String) = Action.async(parse.json) { implicit request =>
    val uuid = UUID.fromString(id)
    request.body.validate[Institution].fold(
      errors => IO.pure(BadRequest(JsError.toJson(errors))),
      institution => catalogRepo.updateInstitution(institution.copy(id = uuid)).as(Ok)
    ).unsafeToFuture()
  }

  def deleteInstitution(id: String) = Action.async { implicit request =>
    val uuid = UUID.fromString(id)
    catalogRepo.deleteInstitution(uuid).as(NoContent).unsafeToFuture()
  }

  def listProgressStates() = Action.async { implicit request =>
    catalogRepo.listProgressStates().unsafeToFuture().map { states =>
      Ok(Json.toJson(states))
    }
  }
}
