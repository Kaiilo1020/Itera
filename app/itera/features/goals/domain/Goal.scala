package itera.features.goals.domain

import java.util.UUID
import java.time.Instant

final case class Goal(
  studentId: UUID,
  objective: String,
  targetDate: Instant,
  goalDate: Option[Instant],
  hoursPerWeek: Int
)

trait GoalRepository[F[_]] {
  def findByStudentId(studentId: UUID): F[Option[Goal]]
  def save(goal: Goal): F[Unit]
  def update(goal: Goal): F[Unit]
}
