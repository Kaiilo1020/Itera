package itera.features.auth.domain

import java.util.UUID

trait AuthRepository[F[_]] {
  def findByEmail(email: Email): F[Option[User]]
  def findById(id: UUID): F[Option[User]]
  def save(user: User): F[Unit]
  def updateLastAccess(id: UUID): F[Unit]
  def findDefaultRoleId(): F[UUID]
}
