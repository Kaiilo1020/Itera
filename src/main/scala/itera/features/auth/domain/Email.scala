package itera.features.auth.domain

import itera.shared.domain.DomainError

final case class Email private (value: String) extends AnyVal

object Email {
  // Factory Pattern for validation as per GEMINI.md
  def create(email: String): Either[DomainError, Email] = {
    val emailRegex = """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
    if (emailRegex.findFirstIn(email).isDefined) Right(new Email(email))
    else Left(DomainError("Invalid email format"))
  }
}
