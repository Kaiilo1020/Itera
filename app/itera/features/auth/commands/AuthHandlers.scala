package itera.features.auth.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.auth.domain.{AuthRepository, Email, HashedPassword, RawPassword, User}
import itera.shared.domain.DomainError
import itera.shared.infrastructure.TokenService

class AuthHandlers[F[_]: Async](
  repo: AuthRepository[F],
  tokenService: TokenService[F]
) {

  def handleRegister(cmd: RegisterCommand): F[Either[DomainError, AuthResponse]] = {
    val result: EitherT[F, DomainError, AuthResponse] = for {
      email        <- EitherT.fromEither[F](Email.create(cmd.email))
      rawPassword  <- EitherT.fromEither[F](RawPassword.create(cmd.password))
      existingUser <- EitherT.right[DomainError](repo.findByEmail(email))
      _            <- EitherT.fromOption[F](Option.when(existingUser.isEmpty)(()), DomainError("Email already in use"))
      roleId       <- EitherT.right[DomainError](repo.findDefaultRoleId())
      hashed       =  HashedPassword.hash(rawPassword)
      user         =  User.create(email, hashed, roleId)
      _            <- EitherT.right[DomainError](repo.save(user))
      token        <- EitherT.right[DomainError](tokenService.issue(user.id, "student"))
    } yield AuthResponse(token, user.id.toString, email.value)

    result.value
  }

  def handleLogin(cmd: LoginCommand): F[Either[DomainError, AuthResponse]] = {
    val result: EitherT[F, DomainError, AuthResponse] = for {
      email       <- EitherT.fromEither[F](Email.create(cmd.email))
      rawPassword <- EitherT.fromEither[F](RawPassword.create(cmd.password))
      user        <- EitherT.fromOptionF[F, DomainError, User](repo.findByEmail(email), DomainError("Invalid credentials"))
      _           <- EitherT.fromOption[F](Option.when(HashedPassword.verify(rawPassword, user.password))(()), DomainError("Invalid credentials"))
      _           <- EitherT.right[DomainError](repo.updateLastAccess(user.id))
      token       <- EitherT.right[DomainError](tokenService.issue(user.id, "student"))
    } yield AuthResponse(token, user.id.toString, email.value)

    result.value
  }
}
