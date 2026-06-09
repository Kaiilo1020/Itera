package itera.features.auth.commands

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import java.util.UUID
import itera.features.auth.domain.{AuthRepository, Email, HashedPassword, RawPassword, User}
import itera.features.profile.domain.{Student, Profile, StudentRepository}
import itera.shared.domain.DomainError
import itera.shared.infrastructure.TokenService

class AuthHandlers[F[_]: Async](
  repo: AuthRepository[F],
  studentRepo: StudentRepository[F],
  tokenService: TokenService[F]
) {

  def handleRegister(cmd: RegisterCommand): F[Either[DomainError, AuthResponse]] = {
    val result: EitherT[F, DomainError, AuthResponse] = for {
      email        <- EitherT.fromEither[F](Email.create(cmd.email))
      rawPassword  <- EitherT.fromEither[F](RawPassword.create(cmd.password))
      existingUser <- EitherT.right[DomainError](repo.findByEmail(email))
      _            <- EitherT.fromOption[F](Option.when(existingUser.isEmpty)(()), DomainError("Email already in use"))
      
      // Use .flatMap and .recover on the effect F
      roleId       <- EitherT(repo.findDefaultRoleId().map(_.asRight[DomainError]).recover {
                        case e => DomainError(s"Error de configuración: ${e.getMessage}").asLeft[UUID]
                      })
      
      hashed       = HashedPassword.hash(rawPassword)
      user         = User.create(email, hashed, roleId)

      // 1. Create Academic Profile context
      student      = Student.create(user.id, cmd.names, cmd.surnames)
      profile      = Profile.empty(student.id)

      // 2. Persist in sequence
      _            <- EitherT.right[DomainError](repo.save(user))
      _            <- EitherT.right[DomainError](studentRepo.saveStudent(student))
      _            <- EitherT.right[DomainError](studentRepo.saveProfile(profile))

      token        <- EitherT.right[DomainError](tokenService.issue(user.id, "ESTUDIANTE"))
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
      token       <- EitherT.right[DomainError](tokenService.issue(user.id, "ESTUDIANTE"))
    } yield AuthResponse(token, user.id.toString, email.value)

    result.value
  }
}
