package itera.domain.repository

import itera.domain.entity.User
import itera.domain.error.DomainError
import itera.domain.valueobject.{Email, EntityId}

trait UserRepository[F[_]] {
  def create(user: User): F[Either[DomainError, Unit]]
  def findById(id: EntityId): F[Either[DomainError, Option[User]]]
  def findByEmail(email: Email): F[Either[DomainError, Option[User]]]
}
