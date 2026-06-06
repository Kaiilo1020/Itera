package itera.application.query.handler

import cats.Monad
import cats.syntax.all._
import itera.application.query.GetProfileQuery
import itera.application.query.dto.ProfileDTO
import itera.domain.error.DomainError
import itera.domain.repository.ProfileRepository

class GetProfileHandler[F[_]: Monad](
  profileRepo: ProfileRepository[F]
) {
  def handle(q: GetProfileQuery): F[Either[DomainError, ProfileDTO]] = {
    profileRepo.findByUserId(q.userId).map {
      case Left(err) => Left(err)
      case Right(None) => Left(DomainError.NotFound("Profile", q.userId.show))
      case Right(Some(profile)) => Right(ProfileDTO.fromEntity(profile))
    }
  }
}
