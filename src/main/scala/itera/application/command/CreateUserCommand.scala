package itera.application.command

import itera.domain.entity.Role
import itera.domain.valueobject.Email

final case class CreateUserCommand(
  email: Email,
  password: String,
  role: Role
)
