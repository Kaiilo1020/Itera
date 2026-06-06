package itera.application.query.handler

import cats.Monad
import cats.syntax.all._
import itera.application.query.GetProgressQuery
import itera.application.query.dto.ProgressDTO
import itera.domain.error.DomainError
import itera.domain.repository.ProgressRepository

class GetProgressHandler[F[_]: Monad](
  progressRepo: ProgressRepository[F]
) {
  def handle(q: GetProgressQuery): F[Either[DomainError, List[ProgressDTO]]] = {
    progressRepo.findByUserId(q.userId).map {
      case Left(err) => Left(err)
      case Right(list) => Right(list.map(ProgressDTO.fromEntity))
    }
  }
}
