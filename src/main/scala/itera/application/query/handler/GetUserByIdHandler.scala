package itera.application.query.handler

import cats.Monad
import cats.syntax.all._
import itera.application.query.GetUserByIdQuery
import itera.application.query.dto.UserDTO
import itera.domain.error.DomainError
import itera.domain.repository.UserRepository

class GetUserByIdHandler[F[_]: Monad](
  userRepo: UserRepository[F]
) {
  def handle(q: GetUserByIdQuery): F[Either[DomainError, UserDTO]] = {
    userRepo.findById(q.userId).map {
      case Left(err) => Left(err)
      case Right(None) => Left(DomainError.NotFound("User", q.userId.show))
      case Right(Some(user)) => Right(UserDTO.fromEntity(user))
    }
  }
}
