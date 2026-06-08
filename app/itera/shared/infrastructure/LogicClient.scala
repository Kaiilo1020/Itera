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
class LogicClient @Inject()(
  ws: WSClient,
  config: Configuration,
  system: ActorSystem
)(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[String]("itera.prologUrl")

  private val breaker = new CircuitBreaker(
    system.scheduler,
    maxFailures = 3,
    callTimeout = 2.seconds,
    resetTimeout = 1.minute
  )

  def getRoadmap(
    studentId: UUID, 
    approvedNodes: List[String], 
    skills: List[String],
    hours: Int,
    pace: String,
    marketGaps: List[play.api.libs.json.JsObject] = Nil
  ): Future[Option[play.api.libs.json.JsValue]] = {
    val payload = Json.obj(
      "student_id" -> studentId.toString,
      "approved_nodes" -> approvedNodes,
      "skills" -> skills,
      "hours" -> hours,
      "pace" -> pace,
      "market_gaps" -> marketGaps
    )

    breaker.withCircuitBreaker {
      ws.url(s"$baseUrl/roadmap")
        .withRequestTimeout(2.seconds)
        .post(payload)
        .map { response =>
          if (response.status == 200) Some(response.json)
          else None
        }
    }.recover {
      case _ => None
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
