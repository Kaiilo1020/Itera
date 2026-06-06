package itera.domain.repository

import itera.domain.entity.Progress
import itera.domain.error.DomainError
import itera.domain.valueobject.EntityId

trait ProgressRepository[F[_]] {
  def create(progress: Progress): F[Either[DomainError, Unit]]
  def findByUserId(userId: EntityId): F[Either[DomainError, List[Progress]]]
  def update(progress: Progress): F[Either[DomainError, Unit]]
}
