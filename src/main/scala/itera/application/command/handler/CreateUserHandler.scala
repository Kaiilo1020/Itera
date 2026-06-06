package itera.application.command.handler

import cats.effect.Sync
import cats.syntax.all._
import java.time.Instant
import itera.application.command.CreateUserCommand
import itera.application.service.PasswordHasher
import itera.domain.entity.{Progress, ProgressStatus, Role, User}
import itera.domain.error.DomainError
import itera.domain.repository.{UserRepository, ProgressRepository}
import itera.domain.valueobject.{EntityId, PasswordHash}

class CreateUserHandler[F[_]: Sync](
  userRepo: UserRepository[F],
  progressRepo: ProgressRepository[F],
  hasher: PasswordHasher[F]
) {
  def handle(cmd: CreateUserCommand): F[Either[DomainError, EntityId]] = {
    for {
      existing <- userRepo.findByEmail(cmd.email)
      result <- existing match {
        case Left(err) =>
          Sync[F].pure(Left[DomainError, EntityId](err))
        case Right(Some(_)) =>
          Sync[F].pure(Left[DomainError, EntityId](DomainError.AlreadyExists("User", "email")))
        case Right(None) =>
          for {
            id        <- EntityId.generate[F]
            now       <- Sync[F].delay(Instant.now())
            hash      <- hasher.hash(cmd.password)
            user       = User(id, cmd.email, PasswordHash(hash), cmd.role, now)
            _         <- userRepo.create(user)
            progress   = Progress(id, 1, 1, ProgressStatus.NotStarted, None)
            _         <- progressRepo.create(progress)
          } yield Right[DomainError, EntityId](id)
      }
    } yield result
  }
}
