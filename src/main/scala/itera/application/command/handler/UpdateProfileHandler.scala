package itera.application.command.handler

import cats.Monad
import cats.syntax.all._
import itera.application.command.UpdateProfileCommand
import itera.domain.entity.Profile
import itera.domain.error.DomainError
import itera.domain.repository.ProfileRepository

class UpdateProfileHandler[F[_]: Monad](
  profileRepo: ProfileRepository[F]
) {
  def handle(cmd: UpdateProfileCommand): F[Either[DomainError, Unit]] = {
    val profile = Profile(cmd.userId, cmd.name, cmd.interests, cmd.educationLevel)
    profileRepo.findByUserId(cmd.userId).flatMap {
      case Left(err) => Monad[F].pure(Left(err))
      case Right(None) =>
        profileRepo.create(profile)
      case Right(Some(_)) =>
        profileRepo.update(profile)
    }
  }
}
