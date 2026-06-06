package itera.domain.entity

import java.time.Instant
import itera.domain.valueobject.EntityId

final case class Progress(
  userId: EntityId,
  act: Int,
  stage: Int,
  status: ProgressStatus,
  completedAt: Option[Instant]
)
