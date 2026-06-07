package itera.features.auth.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.auth.domain.{AuthRepository, Email, HashedPassword, RawPassword, User}
import itera.shared.domain.DomainError
import itera.shared.infrastructure.TokenService
import java.util.UUID

class AuthHandlers[F[_]: Async](
  repo: AuthRepository[F],
  tokenService: TokenService[F]
) {

  def handleRegister(cmd: RegisterCommand): F[Either[DomainError, AuthResponse]] = {
    val result = for {
      email        <- EitherT.fromEither[F](Email.create(cmd.email))
      rawPassword  <- EitherT.fromEither[F](RawPassword.create(cmd.password))
      existingUser <- EitherT.right(repo.findByEmail(email))
      _            <- EitherT.fromOption[F](Option.when(existingUser.isEmpty)(()), DomainError("Email already in use"))
      roleId       <- EitherT.right(repo.findDefaultRoleId())
      hashed       =  HashedPassword.hash(rawPassword)
      user         =  User.create(email, hashed, roleId)
      _            <- EitherT.right(repo.save(user))
      token        <- EitherT.right(tokenService.issue(user.id, "student"))
    } yield AuthResponse(token, user.id.toString, email.value)

    result.value
  }

  def handleLogin(cmd: LoginCommand): F[Either[DomainError, AuthResponse]] = {
    val result = for {
      email       <- EitherT.fromEither[F](Email.create(cmd.email))
      rawPassword <- EitherT.fromEither[F](RawPassword.create(cmd.password))
      user        <- EitherT.fromOptionF(repo.findByEmail(email), DomainError("Invalid credentials"))
      _           <- EitherT.fromOption[F](Option.when(HashedPassword.verify(rawPassword, user.password))(()), DomainError("Invalid credentials"))
      _           <- EitherT.right(repo.updateLastAccess(user.id))
      token       <- EitherT.right(tokenService.issue(user.id, "student")) // role should come from DB in a real app
    } yield AuthResponse(token, user.id.toString, email.value)

    result.value
  }
}
