package itera.shared.infrastructure

import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.ws.WSClient
import play.api.Configuration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.libs.json.Json
import java.util.UUID
import akka.pattern.CircuitBreaker
import akka.actor.ActorSystem
import scala.concurrent.duration._
import play.api.Logging

@Singleton
class IAClient @Inject()(
  ws: WSClient,
  config: Configuration,
  system: ActorSystem
)(implicit ec: ExecutionContext) extends Logging {

  private val baseUrl = config.get[String]("itera.iaApiUrl")

  // Circuit Breaker for the AI/Python service
  private val breaker = new CircuitBreaker(
    system.scheduler,
    maxFailures = 3,
    callTimeout = 5.seconds, // AI calls might take longer
    resetTimeout = 1.minute
  ).onOpen(logger.warn("Circuit Breaker for AI Service opened!"))
    .onClose(logger.info("Circuit Breaker for AI Service closed."))

  def getMatchScore(studentId: UUID, skills: List[String], goal: String): Future[Option[play.api.libs.json.JsValue]] = {
    val payload = Json.obj(
      "student_id" -> studentId.toString,
      "skills" -> skills,
      "objetivo" -> goal
    )

    breaker.withCircuitBreaker {
      ws.url(s"$baseUrl/match/evaluate")
        .withRequestTimeout(5.seconds)
        .post(payload)
        .map { response =>
          if (response.status == 200) Some(response.json)
          else None
        }
    }.recover {
      case _: Exception =>
        logger.error("AI Service call failed. Degrading gracefully.")
        None
    }
  }

  def checkHealth(): Future[Boolean] = {
    ws.url(s"$baseUrl/health")
      .withRequestTimeout(1.second)
      .get()
      .map(_.status == 200)
      .recover { case _ => false }
  }
}
