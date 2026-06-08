package itera.features.goals.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.goals.domain.{Goal, GoalRepository}
import itera.features.profile.domain.StudentRepository
import itera.shared.domain.DomainError
import java.time.Instant

class GoalHandlers[F[_]: Async](
  goalRepo: GoalRepository[F],
  studentRepo: StudentRepository[F]
) {

  def handleSetGoals(cmd: SetGoalsCommand): F[Either[DomainError, Unit]] = {
    val result: EitherT[F, DomainError, Unit] = for {
      student <- EitherT.fromOptionF[F, DomainError, itera.features.profile.domain.Student](studentRepo.findByUserId(cmd.userId), DomainError("Student profile not found"))
      existingGoal <- EitherT.right[DomainError](goalRepo.findByStudentId(student.id))
      
      goal = Goal(
        studentId = student.id,
        objective = cmd.objective,
        targetDate = Instant.now(),
        goalDate = cmd.goalDate,
        hoursPerWeek = cmd.hoursPerWeek
      )
      
      _ <- EitherT.right[DomainError](if (existingGoal.isDefined) goalRepo.update(goal) else goalRepo.save(goal))
    } yield ()

    result.value
  }
}
