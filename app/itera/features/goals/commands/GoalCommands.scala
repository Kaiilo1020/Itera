package itera.features.goals.commands

import java.util.UUID
import java.time.Instant

final case class SetGoalsCommand(
  userId: UUID,
  objective: String,
  hoursPerWeek: Int,
  goalDate: Option[Instant]
)
