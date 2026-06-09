package itera.features.auth.commands

import itera.features.auth.domain.{Email, RawPassword}
import itera.shared.domain.DomainError

final case class RegisterCommand(
  names: String,
  surnames: String,
  email: String,
  password: String
)

final case class LoginCommand(
  email: String,
  password: String
)

// DTO for Response
final case class AuthResponse(
  token: String,
  userId: String,
  email: String
)
