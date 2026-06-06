package itera.domain.repository

import itera.domain.entity.Profile
import itera.domain.error.DomainError
import itera.domain.valueobject.EntityId

trait ProfileRepository[F[_]] {
  def create(profile: Profile): F[Either[DomainError, Unit]]
  def findByUserId(userId: EntityId): F[Either[DomainError, Option[Profile]]]
  def update(profile: Profile): F[Either[DomainError, Unit]]
}
