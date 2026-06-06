package itera.presentation.dto

import itera.domain.entity.Role
import itera.domain.valueobject.Email
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateUserRequest(
  email: String,
  password: String,
  role: String
) {
  def toDomain: Either[String, (Email, Role)] = {
    for {
      email <- Email.fromString(email)
      role  <- role.toLowerCase match {
        case "admin"     => Right(Role.Admin)
        case "student"   => Right(Role.Student)
        case "counselor" => Right(Role.Counselor)
        case other       => Left(s"Invalid role: $other")
      }
    } yield (email, role)
  }
}

object CreateUserRequest {
  implicit val decoder: Decoder[CreateUserRequest] = deriveDecoder[CreateUserRequest]
  implicit val encoder: Encoder[CreateUserRequest] = deriveEncoder[CreateUserRequest]
}
