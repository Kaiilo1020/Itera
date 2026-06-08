package controllers

import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import itera.features.auth.commands.{AuthHandlers, AuthResponse, RegisterCommand}
import itera.shared.domain.DomainError
import cats.effect.IO
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.ArgumentMatchers.any
import akka.stream.Materializer

/**
 * Test suite for AuthController.
 * Demonstrates how to test a controller that uses IO (Cats Effect).
 */
class AuthControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with Injecting {

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(Map(
      "play.http.secret.key" -> "abcdefghijklmnopqrstuvwxyz0123456789ABCDEF",
      "db.default.driver" -> "org.h2.Driver",
      "db.default.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "play.modules.disabled" -> Seq("modules.FlywayModule")
    ))
    .build()

  implicit lazy val materializer: Materializer = inject[Materializer]

  "AuthController POST /api/core/auth/register" should {

    "return Created (201) when registration is successful" in {
      // 1. Mock the Handlers
      val mockHandlers = mock[AuthHandlers[IO]]
      val expectedResponse = AuthResponse("fake-token", "user-uuid", "test@itera.com")
      
      // Define behavior: return a Right(AuthResponse) wrapped in IO
      when(mockHandlers.handleRegister(any[RegisterCommand]))
        .thenReturn(IO.pure(Right(expectedResponse)))

      // 2. Instantiate controller
      val controller = new AuthController(stubControllerComponents(), mockHandlers)

      // 3. Execute
      val request = FakeRequest(POST, "/api/core/auth/register")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withJsonBody(Json.obj(
          "email" -> "test@itera.com",
          "password" -> "Password123"
        ))
      
      val result = call(controller.register(), request)

      // 4. Assert
      status(result) mustBe CREATED
      val json = contentAsJson(result)
      (json \ "token").as[String] mustBe "fake-token"
      (json \ "email").as[String] mustBe "test@itera.com"
    }

    "return BadRequest (400) when registration fails due to domain error (e.g. user exists)" in {
      val mockHandlers = mock[AuthHandlers[IO]]
      
      when(mockHandlers.handleRegister(any[RegisterCommand]))
        .thenReturn(IO.pure(Left(DomainError("Email already in use"))))

      val controller = new AuthController(stubControllerComponents(), mockHandlers)

      val request = FakeRequest(POST, "/api/core/auth/register")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withJsonBody(Json.obj(
          "email" -> "existing@itera.com",
          "password" -> "Password123"
        ))
      
      val result = call(controller.register(), request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Email already in use"
    }
  }
}
