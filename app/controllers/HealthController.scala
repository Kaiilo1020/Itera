package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents
import play.api.mvc.Action
import play.api.libs.json.Json
import itera.shared.infrastructure.{LogicClient, IAClient}
import scala.concurrent.ExecutionContext

@Singleton
class HealthController @Inject()(
  val controllerComponents: ControllerComponents,
  logicClient: LogicClient,
  iaClient: IAClient
)(implicit ec: ExecutionContext) extends BaseController {

  def health() = Action.async { implicit request =>
    for {
      logicHealthy <- logicClient.checkHealth()
      iaHealthy    <- iaClient.checkHealth()
    } yield {
      Ok(Json.obj(
        "status" -> "ok",
        "service" -> "itera-scala",
        "framework" -> "Play Framework 2.9",
        "dependencies" -> Json.obj(
          "logic_engine" -> (if (logicHealthy) "connected" else "offline"),
          "ia_service" -> (if (iaHealthy) "connected" else "offline")
        )
      ))
    }
  }
}
