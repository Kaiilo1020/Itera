package itera.features.progress.domain

import java.util.UUID
import java.time.Instant

final case class Progress(
  id: UUID,
  studentId: UUID,
  nodeId: String,
  statusId: UUID,
  grade: Option[BigDecimal],
  evidence: Option[String],
  attempts: Int,
  origin: String,
  startDate: Option[Instant],
  endDate: Option[Instant]
)

object Progress {
  def initialize(studentId: UUID, nodeId: String, statusId: UUID): Progress = {
    Progress(
      id = UUID.randomUUID(),
      studentId = studentId,
      nodeId = nodeId,
      statusId = statusId,
      grade = None,
      evidence = None,
      attempts = 0,
      origin = "manual",
      startDate = Some(Instant.now()),
      endDate = None
    )
  }
}

trait ProgressRepository[F[_]] {
  def findByStudentAndNode(studentId: UUID, nodeId: String): F[Option[Progress]]
  def save(progress: Progress): F[Unit]
  def update(progress: Progress): F[Unit]
  def findStatusIdByName(name: String): F[UUID]
}
