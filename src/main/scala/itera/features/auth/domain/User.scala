package itera.features.auth.domain

import java.util.UUID
import java.time.Instant

final case class User(
  id: UUID,
  email: Email,
  password: HashedPassword,
  roleId: UUID,
  plan: String,
  status: String,
  createdAt: Instant,
  lastAccess: Option[Instant]
)

object User {
  // Factory Pattern for new users
  def create(
    email: Email,
    password: HashedPassword,
    roleId: UUID
  ): User = {
    User(
      id = UUID.randomUUID(),
      email = email,
      password = password,
      roleId = roleId,
      plan = "basic",
      status = "active",
      createdAt = Instant.now(),
      lastAccess = None
    )
  }
}
