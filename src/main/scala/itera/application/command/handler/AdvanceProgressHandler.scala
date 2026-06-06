package itera.application.command.handler

import cats.Monad
import cats.syntax.all._
import java.time.Instant
import itera.application.command.AdvanceProgressCommand
import itera.domain.entity.{Progress, ProgressStatus}
import itera.domain.error.DomainError
import itera.domain.repository.ProgressRepository

class AdvanceProgressHandler[F[_]: Monad](
  progressRepo: ProgressRepository[F]
) {
  def handle(cmd: AdvanceProgressCommand): F[Either[DomainError, Unit]] = {
    progressRepo.findByUserId(cmd.userId).flatMap {
      case Left(err) => Monad[F].pure(Left(err))
      case Right(progressList) =>
        progressList.find(p => p.act == cmd.act && p.stage == cmd.stage) match {
          case None =>
            Monad[F].pure(Left[DomainError, Unit](DomainError.NotFound("Progress", s"act=${cmd.act},stage=${cmd.stage}")))
          case Some(existing) =>
            val updated = existing.copy(
              status = ProgressStatus.Completed,
              completedAt = Some(Instant.now())
            )
            progressRepo.update(updated)
        }
    }
  }
}
