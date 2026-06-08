package itera.features.progress.infrastructure

import cats.effect.Async
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import itera.features.progress.domain.{Progress, ProgressRepository}
import java.util.UUID

class DoobieProgressRepository[F[_]: Async](xa: Transactor[F]) extends ProgressRepository[F] {

  override def findByStudentAndNode(studentId: UUID, nodeId: String): F[Option[Progress]] =
    sql"SELECT id, student_id, node_id, status_id, grade, evidence, attempts, origin, start_date, end_date FROM progress WHERE student_id = $studentId AND node_id = $nodeId"
      .query[Progress]
      .option
      .transact(xa)

  override def save(progress: Progress): F[Unit] =
    sql"""
      INSERT INTO progress (id, student_id, node_id, status_id, grade, evidence, attempts, origin, start_date, end_date)
      VALUES (${progress.id}, ${progress.studentId}, ${progress.nodeId}, ${progress.statusId}, ${progress.grade}, ${progress.evidence}, ${progress.attempts}, ${progress.origin}, ${progress.startDate}, ${progress.endDate})
    """.update.run.transact(xa).void

  override def update(progress: Progress): F[Unit] =
    sql"""
      UPDATE progress
      SET status_id = ${progress.statusId}, grade = ${progress.grade}, evidence = ${progress.evidence}, attempts = ${progress.attempts}, end_date = ${progress.endDate}
      WHERE id = ${progress.id}
    """.update.run.transact(xa).void

  override def findStatusIdByName(name: String): F[UUID] =
    sql"SELECT id FROM progress_states WHERE name = $name LIMIT 1"
      .query[UUID]
      .unique
      .transact(xa)
}
