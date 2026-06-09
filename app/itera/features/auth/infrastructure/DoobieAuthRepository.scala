package itera.features.auth.infrastructure

import cats.effect.Async
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import itera.features.auth.domain.{AuthRepository, Email, HashedPassword, User}
import java.util.UUID
import java.time.Instant

class DoobieAuthRepository[F[_]: Async](xa: Transactor[F]) extends AuthRepository[F] {

  // Custom mappings for Value Objects
  implicit val emailMeta: Meta[Email] = Meta[String].imap(Email.apply)(_.value)
  implicit val hashedPasswordMeta: Meta[HashedPassword] = Meta[String].imap(HashedPassword.fromHash)(_.value)

  override def findByEmail(email: Email): F[Option[User]] =
    sql"SELECT id, email, password, role_id, plan, status, created_at, last_access FROM users WHERE email = ${email.value}"
      .query[User]
      .option
      .transact(xa)

  override def findById(id: UUID): F[Option[User]] =
    sql"SELECT id, email, password, role_id, plan, status, created_at, last_access FROM users WHERE id = $id"
      .query[User]
      .option
      .transact(xa)

  override def save(user: User): F[Unit] =
    sql"""
      INSERT INTO users (id, email, password, role_id, plan, status, created_at, last_access)
      VALUES (${user.id}, ${user.email}, ${user.password}, ${user.roleId}, ${user.plan}, ${user.status}, ${user.createdAt}, ${user.lastAccess})
    """.update.run.transact(xa).void

  override def updateLastAccess(id: UUID): F[Unit] =
    sql"UPDATE users SET last_access = ${Instant.now()} WHERE id = $id"
      .update.run.transact(xa).void

  override def findDefaultRoleId(): F[UUID] =
    sql"SELECT id FROM roles WHERE name = 'ESTUDIANTE' LIMIT 1"
      .query[UUID]
      .option
      .transact(xa)
      .flatMap {
        case Some(id) => id.pure[F]
        case None => Async[F].raiseError(new RuntimeException("Default 'ESTUDIANTE' role not found in database. Please run migrations/seeds."))
      }
}
