package itera.infrastructure.security

import itera.domain.valueobject.EntityId

trait TokenService[F[_]] {
  def issue(userId: EntityId, role: String): F[String]
  def validate(token: String): F[Either[String, (EntityId, String)]]
}
