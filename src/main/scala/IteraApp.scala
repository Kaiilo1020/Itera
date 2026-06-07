import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._
import cats.syntax.semigroupk._
import com.comcast.ip4s._
import itera.Module

object IteraApp extends IOApp.Simple {

  def run: IO[Unit] = {
    Module.resources[IO].use { resources =>
      val app = Module.build[IO](resources.xa, resources.config)

      val httpApp = Router(
        "/api/v1" -> (
          app.authRoutes.routes <+>
          app.healthRoutes.routes
        )
      ).orNotFound

      EmberServerBuilder.default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build
        .use { server =>
          IO.println(s"Server running at ${server.address}") *>
          IO.never
        }
    }
  }
}
