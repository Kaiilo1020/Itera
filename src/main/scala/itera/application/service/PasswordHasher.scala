package itera.application.service

trait PasswordHasher[F[_]] {
  def hash(plain: String): F[String]
  def verify(plain: String, hash: String): F[Boolean]
}
