package itera.infrastructure.persistence

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import java.time.Instant
import java.util.UUID
import itera.domain.entity.{Role, User}
import itera.domain.error.DomainError
import itera.domain.repository.UserRepository
import itera.domain.valueobject.{Email, EntityId, PasswordHash}

class UserRepositoryImpl[F[_]: MonadCancelThrow](xa: Transactor[F]) extends UserRepository[F] {

  def create(user: User): F[Either[DomainError, Unit]] = {
    sql"""
      INSERT INTO users (id, email, password_hash, role, created_at)
      VALUES (${user.id.value}, ${user.email.show}, ${user.passwordHash.value}, ${roleToString(user.role)}, ${user.createdAt})
    """.update.run.transact(xa).attempt.map {
      case Right(_) => Right(())
      case Left(e) if e.getMessage.toLowerCase.contains("duplicate") =>
        Left(DomainError.AlreadyExists("User", "email"))
      case Left(e) =>
        Left(DomainError.InternalError(e.getMessage))
    }
  }

  def findById(id: EntityId): F[Either[DomainError, Option[User]]] = {
    sql"""
      SELECT id, email, password_hash, role, created_at
      FROM users
      WHERE id = ${id.value}
    """.query[(UUID, String, String, String, Instant)].option.transact(xa).attempt.map {
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
      case Right(None) => Right(None)
      case Right(Some((uuid, email, hash, role, createdAt))) =>
        Email.fromString(email) match {
          case Left(err) => Left(DomainError.ValidationError(err))
          case Right(emailV) =>
            Right(Some(User(EntityId(uuid), emailV, PasswordHash(hash), stringToRole(role), createdAt)))
        }
    }
  }

  def findByEmail(email: Email): F[Either[DomainError, Option[User]]] = {
    sql"""
      SELECT id, email, password_hash, role, created_at
      FROM users
      WHERE email = ${email.show}
    """.query[(UUID, String, String, String, Instant)].option.transact(xa).attempt.map {
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
      case Right(None) => Right(None)
      case Right(Some((uuid, emailS, hash, role, createdAt))) =>
        Email.fromString(emailS) match {
          case Left(err) => Left(DomainError.ValidationError(err))
          case Right(emailV) =>
            Right(Some(User(EntityId(uuid), emailV, PasswordHash(hash), stringToRole(role), createdAt)))
        }
    }
  }

  private def roleToString(role: Role): String = role match {
    case Role.Admin     => "admin"
    case Role.Student   => "student"
    case Role.Counselor => "counselor"
  }

  private def stringToRole(s: String): Role = s match {
    case "admin"     => Role.Admin
    case "student"   => Role.Student
    case "counselor" => Role.Counselor
    case _           => Role.Student
  }
}
