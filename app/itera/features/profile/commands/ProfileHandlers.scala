package itera.features.profile.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.profile.domain.{Profile, Student, StudentRepository}
import itera.shared.domain.DomainError

class ProfileHandlers[F[_]: Async](repo: StudentRepository[F]) {

  def handleInitialize(cmd: InitializeProfileCommand): F[Either[DomainError, Unit]] = {
    val result: EitherT[F, DomainError, Unit] = for {
      existing <- EitherT.right[DomainError](repo.findByUserId(cmd.userId))
      _        <- EitherT.fromOption[F](Option.when(existing.isEmpty)(()), DomainError("Profile already initialized"))
      student  =  Student.create(cmd.userId, cmd.names, cmd.surnames, cmd.institutionId, cmd.cycle)
      profile  =  Profile.empty(student.id).copy(skills = cmd.skills)
      _        <- EitherT.right[DomainError](repo.saveStudent(student))
      _        <- EitherT.right[DomainError](repo.saveProfile(profile))
    } yield ()

    result.value
  }

  def handleUpdate(cmd: UpdateProfileCommand): F[Either[DomainError, Unit]] = {
    val result: EitherT[F, DomainError, Unit] = for {
      student <- EitherT.fromOptionF[F, DomainError, Student](repo.findByUserId(cmd.userId), DomainError("Student not found"))
      profile <- EitherT.fromOptionF[F, DomainError, Profile](repo.findProfileByStudentId(student.id), DomainError("Profile not found"))
      
      updatedStudent = student.copy(
        names = cmd.names.getOrElse(student.names),
        surnames = cmd.surnames.getOrElse(student.surnames),
        institutionId = cmd.institutionId.orElse(student.institutionId),
        cycle = cmd.cycle.getOrElse(student.cycle),
        academicGoal = cmd.academicGoal.getOrElse(student.academicGoal)
      )
      
      updatedProfile = profile.copy(
        skills = cmd.skills.getOrElse(profile.skills),
        photo = cmd.photo.orElse(profile.photo)
      )
      
      _ <- EitherT.right[DomainError](repo.updateStudent(updatedStudent))
      _ <- EitherT.right[DomainError](repo.updateProfile(updatedProfile))
    } yield ()

    result.value
  }
}
