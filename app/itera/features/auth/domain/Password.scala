package itera.features.auth.domain

import itera.shared.domain.DomainError
import com.github.t3hnar.bcrypt._

final case class RawPassword private (value: String) extends AnyVal

object RawPassword {
  def create(password: String): Either[DomainError, RawPassword] = {
    if (password.length >= 8) Right(new RawPassword(password))
    else Left(DomainError("Password must be at least 8 characters long"))
  }
}

final case class HashedPassword private (value: String) extends AnyVal

object HashedPassword {
  def fromHash(hash: String): HashedPassword = new HashedPassword(hash)

  // Factory/Converter from RawPassword
  def hash(raw: RawPassword): HashedPassword = {
    // Correct bcrypt syntax for t3hnar
    new HashedPassword(raw.value.boundedBcrypt)
  }

  def verify(raw: RawPassword, hashed: HashedPassword): Boolean = {
    // Correct bcrypt verification syntax for t3hnar
    raw.value.isBcryptedBounded(hashed.value)
  }
}
