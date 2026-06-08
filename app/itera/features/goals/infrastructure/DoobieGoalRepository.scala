package itera.features.goals.infrastructure

import cats.effect.Async
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import itera.features.goals.domain.{Goal, GoalRepository}
import java.util.UUID

class DoobieGoalRepository[F[_]: Async](xa: Transactor[F]) extends GoalRepository[F] {

  override def findByStudentId(studentId: UUID): F[Option[Goal]] =
    sql"SELECT student_id, objective, target_date, goal_date, hours_per_week FROM study_plans WHERE student_id = $studentId"
      .query[Goal]
      .option
      .transact(xa)

  override def save(goal: Goal): F[Unit] =
    sql"""
      INSERT INTO study_plans (student_id, objective, target_date, goal_date, hours_per_week)
      VALUES (${goal.studentId}, ${goal.objective}, ${goal.targetDate}, ${goal.goalDate}, ${goal.hoursPerWeek})
    """.update.run.transact(xa).void

  override def update(goal: Goal): F[Unit] =
    sql"""
      UPDATE study_plans
      SET objective = ${goal.objective}, target_date = ${goal.targetDate}, goal_date = ${goal.goalDate}, hours_per_week = ${goal.hoursPerWeek}
      WHERE student_id = ${goal.studentId}
    """.update.run.transact(xa).void
}
