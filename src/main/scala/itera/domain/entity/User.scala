package itera.domain.entity

import java.time.Instant
import itera.domain.valueobject.{Email, EntityId, PasswordHash}

final case class User(
  id: EntityId,
  email: Email,
  passwordHash: PasswordHash,
  role: Role,
  createdAt: Instant
)
