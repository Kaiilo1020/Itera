package itera.infrastructure.security

import cats.effect.Sync
import cats.syntax.all._
import com.github.t3hnar.bcrypt._
import itera.application.service.PasswordHasher

class PasswordHasherImpl[F[_]: Sync] extends PasswordHasher[F] {

  def hash(plain: String): F[String] =
    Sync[F].delay(plain.boundedBcrypt)

  def verify(plain: String, hash: String): F[Boolean] =
    Sync[F].delay(plain.isBcrypted(hash))
}
