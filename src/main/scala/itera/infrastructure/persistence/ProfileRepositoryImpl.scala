package itera.infrastructure.persistence

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import java.util.UUID
import itera.domain.entity.{EducationLevel, Profile}
import itera.domain.error.DomainError
import itera.domain.repository.ProfileRepository
import itera.domain.valueobject.EntityId

class ProfileRepositoryImpl[F[_]: MonadCancelThrow](xa: Transactor[F]) extends ProfileRepository[F] {

  def create(profile: Profile): F[Either[DomainError, Unit]] = {
    sql"""
      INSERT INTO profiles (user_id, name, interests, education_level)
      VALUES (${profile.userId.value}, ${profile.name}, ${profile.interests.toArray[String]}, ${eduToString(profile.educationLevel)})
    """.update.run.transact(xa).attempt.map {
      case Right(_) => Right(())
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
    }
  }

  def findByUserId(userId: EntityId): F[Either[DomainError, Option[Profile]]] = {
    sql"""
      SELECT user_id, name, interests, education_level
      FROM profiles
      WHERE user_id = ${userId.value}
    """.query[(UUID, String, List[String], String)].option.transact(xa).attempt.map {
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
      case Right(None) => Right(None)
      case Right(Some((uuid, name, interests, edu))) =>
        Right(Some(Profile(EntityId(uuid), name, interests, stringToEdu(edu))))
    }
  }

  def update(profile: Profile): F[Either[DomainError, Unit]] = {
    sql"""
      UPDATE profiles
      SET name = ${profile.name},
          interests = ${profile.interests.toArray[String]},
          education_level = ${eduToString(profile.educationLevel)}
      WHERE user_id = ${profile.userId.value}
    """.update.run.transact(xa).attempt.map {
      case Right(_) => Right(())
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
    }
  }

  private def eduToString(e: EducationLevel): String = e match {
    case EducationLevel.HighSchool => "high_school"
    case EducationLevel.Bachelor   => "bachelor"
    case EducationLevel.Master     => "master"
    case EducationLevel.Doctorate  => "doctorate"
    case EducationLevel.Other      => "other"
  }

  private def stringToEdu(s: String): EducationLevel = s match {
    case "high_school" => EducationLevel.HighSchool
    case "bachelor"    => EducationLevel.Bachelor
    case "master"      => EducationLevel.Master
    case "doctorate"   => EducationLevel.Doctorate
    case _             => EducationLevel.Other
  }
}
