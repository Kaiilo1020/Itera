package itera.infrastructure.persistence

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import java.time.Instant
import java.util.UUID
import itera.domain.entity.{Progress, ProgressStatus}
import itera.domain.error.DomainError
import itera.domain.repository.ProgressRepository
import itera.domain.valueobject.EntityId

class ProgressRepositoryImpl[F[_]: MonadCancelThrow](xa: Transactor[F]) extends ProgressRepository[F] {

  def create(progress: Progress): F[Either[DomainError, Unit]] = {
    sql"""
      INSERT INTO progress (user_id, act, stage, status, completed_at)
      VALUES (${progress.userId.value}, ${progress.act}, ${progress.stage}, ${statusToString(progress.status)}, ${progress.completedAt})
    """.update.run.transact(xa).attempt.map {
      case Right(_) => Right(())
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
    }
  }

  def findByUserId(userId: EntityId): F[Either[DomainError, List[Progress]]] = {
    sql"""
      SELECT user_id, act, stage, status, completed_at
      FROM progress
      WHERE user_id = ${userId.value}
    """.query[(UUID, Int, Int, String, Option[Instant])].to[List].transact(xa).attempt.map {
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
      case Right(rows) =>
        Right(rows.map { case (uuid, act, stage, status, completedAt) =>
          Progress(EntityId(uuid), act, stage, stringToStatus(status), completedAt)
        })
    }
  }

  def update(progress: Progress): F[Either[DomainError, Unit]] = {
    sql"""
      UPDATE progress
      SET status = ${statusToString(progress.status)},
          completed_at = ${progress.completedAt}
      WHERE user_id = ${progress.userId.value}
        AND act = ${progress.act}
        AND stage = ${progress.stage}
    """.update.run.transact(xa).attempt.map {
      case Right(_) => Right(())
      case Left(e) => Left(DomainError.InternalError(e.getMessage))
    }
  }

  private def statusToString(s: ProgressStatus): String = s match {
    case ProgressStatus.NotStarted => "not_started"
    case ProgressStatus.InProgress => "in_progress"
    case ProgressStatus.Completed  => "completed"
  }

  private def stringToStatus(s: String): ProgressStatus = s match {
    case "not_started" => ProgressStatus.NotStarted
    case "in_progress" => ProgressStatus.InProgress
    case "completed"   => ProgressStatus.Completed
    case _             => ProgressStatus.NotStarted
  }
}
