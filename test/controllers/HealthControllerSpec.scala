package controllers

import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import itera.shared.infrastructure.{IAClient, LogicClient}
import scala.concurrent.Future
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.Materializer

/**
 * Example Test Suite for HealthController.
 * This demonstrates how to test Play controllers using ScalaTest and Mockito.
 */
class HealthControllerSpec extends PlaySpec with GuiceOneAppPerTest with MockitoSugar with Injecting {

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(Map(
      "play.http.secret.key" -> "abcdefghijklmnopqrstuvwxyz0123456789ABCDEF",
      "db.default.driver" -> "org.h2.Driver",
      "db.default.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "play.modules.disabled" -> Seq("modules.FlywayModule")
    ))
    .build()

  implicit lazy val materializer: Materializer = inject[Materializer]

  "HealthController GET /health" should {

    "return OK with status 'ok' and correct dependency states" in {
      // 1. Setup Mocks for external clients
      val mockLogicClient = mock[LogicClient]
      val mockIAClient = mock[IAClient]

      // 2. Define expected behavior
      when(mockLogicClient.checkHealth()).thenReturn(Future.successful(true))
      when(mockIAClient.checkHealth()).thenReturn(Future.successful(false))

      // 3. Instantiate controller with stub components and mocks
      // Note: stubControllerComponents() provides the necessary Play infrastructure for testing
      val controller = new HealthController(
        stubControllerComponents(), 
        mockLogicClient, 
        mockIAClient
      )

      // 4. Execute the request
      val healthResponse = controller.health().apply(FakeRequest(GET, "/health"))

      // 5. Assertions
      status(healthResponse) mustBe OK
      contentType(healthResponse) mustBe Some("application/json")
      
      val json = contentAsJson(healthResponse)
      (json \ "status").as[String] mustBe "ok"
      (json \ "service").as[String] mustBe "itera-scala"
      
      // Verify dependency reporting
      (json \ "dependencies" \ "logic_engine").as[String] mustBe "connected"
      (json \ "dependencies" \ "ia_service").as[String] mustBe "offline"
    }

    "return OK even if logic engine is offline (graceful degradation)" in {
      val mockLogicClient = mock[LogicClient]
      val mockIAClient = mock[IAClient]

      when(mockLogicClient.checkHealth()).thenReturn(Future.successful(false))
      when(mockIAClient.checkHealth()).thenReturn(Future.successful(true))

      val controller = new HealthController(stubControllerComponents(), mockLogicClient, mockIAClient)
      val healthResponse = controller.health().apply(FakeRequest(GET, "/health"))

      status(healthResponse) mustBe OK
      val json = contentAsJson(healthResponse)
      (json \ "dependencies" \ "logic_engine").as[String] mustBe "offline"
      (json \ "dependencies" \ "ia_service").as[String] mustBe "connected"
    }
  }
}
