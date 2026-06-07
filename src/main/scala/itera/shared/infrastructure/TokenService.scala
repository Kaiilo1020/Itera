package itera.shared.infrastructure

import java.util.UUID

trait TokenService[F[_]] {
  def issue(userId: UUID, role: String): F[String]
  def validate(token: String): F[Either[String, (UUID, String)]]
}
