package itera.presentation.route

import cats.effect.Sync
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class HealthRoutes[F[_]: Sync] extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "health" =>
      Ok(Map("status" -> "ok").asJson)
  }
}
