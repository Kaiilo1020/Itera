package itera.domain.valueobject

import java.util.regex.Pattern

final case class Email private(value: String) extends AnyVal {
  def show: String = value
}

object Email {
  private val EmailPattern: Pattern =
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

  def fromString(s: String): Either[String, Email] = {
    val trimmed = s.trim.toLowerCase
    if (trimmed.nonEmpty && EmailPattern.matcher(trimmed).matches())
      Right(new Email(trimmed))
    else
      Left(s"Invalid email: $s")
  }
}
