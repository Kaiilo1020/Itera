package itera.features.progress.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.progress.domain.{Progress, ProgressRepository}
import itera.features.profile.domain.{Profile, StudentRepository}
import itera.shared.domain.DomainError
import java.time.Instant
import io.circe.Json
import io.circe.syntax._

class ProgressHandlers[F[_]: Async](
  progressRepo: ProgressRepository[F],
  studentRepo: StudentRepository[F]
) {

  def handleSubmitEvidence(cmd: SubmitEvidenceCommand): F[Either[DomainError, Unit]] = {
    val result: EitherT[F, DomainError, Unit] = for {
      student <- EitherT.fromOptionF[F, DomainError, itera.features.profile.domain.Student](studentRepo.findByUserId(cmd.userId), DomainError("Student not found"))
      statusId <- EitherT.right[DomainError](progressRepo.findStatusIdByName("in_progress"))
      
      existing <- EitherT.right[DomainError](progressRepo.findByStudentAndNode(student.id, cmd.nodeId))
      
      progress = existing match {
        case Some(p) => p.copy(evidence = Some(cmd.evidenceUrl), statusId = statusId, attempts = p.attempts + 1)
        case None => Progress.initialize(student.id, cmd.nodeId, statusId).copy(evidence = Some(cmd.evidenceUrl), attempts = 1)
      }
      
      _ <- EitherT.right[DomainError](if (existing.isDefined) progressRepo.update(progress) else progressRepo.save(progress))
    } yield ()

    result.value
  }

  def handleApproveNode(cmd: ApproveNodeCommand): F[Either[DomainError, Unit]] = {
    val result: EitherT[F, DomainError, Unit] = for {
      student <- EitherT.fromOptionF[F, DomainError, itera.features.profile.domain.Student](studentRepo.findByUserId(cmd.userId), DomainError("Student not found"))
      statusId <- EitherT.right[DomainError](progressRepo.findStatusIdByName("passed"))
      
      existing <- EitherT.fromOptionF[F, DomainError, Progress](progressRepo.findByStudentAndNode(student.id, cmd.nodeId), DomainError("Progress record not found"))
      
      updated = existing.copy(
        statusId = statusId,
        grade = Some(cmd.grade),
        endDate = Some(Instant.now())
      )
      
      _ <- EitherT.right[DomainError](progressRepo.update(updated))
      
      // RF-05: Update Experience & Badges
      profile <- EitherT.fromOptionF[F, DomainError, Profile](studentRepo.findProfileByStudentId(student.id), DomainError("Profile not found"))
      
      // EXP Logic
      newExp = profile.experience + 100
      
      // Badge Logic
      newBadges = if (profile.badges.isEmpty || profile.badges.get.asArray.exists(_.isEmpty)) {
        Some(Json.arr(Json.obj(
          "id" -> Json.fromString("first_node"), 
          "name" -> Json.fromString("Node Explorer"), 
          "date" -> Json.fromString(Instant.now().toString)
        )))
      } else {
        profile.badges
      }

      updatedProfile: Profile = profile.copy(
        experience = newExp,
        badges = newBadges
      )
      _ <- EitherT.right[DomainError](studentRepo.updateProfile(updatedProfile))
      
    } yield ()

    result.value
  }
}
