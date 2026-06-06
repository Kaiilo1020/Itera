package itera.application.query.dto

import java.time.Instant
import itera.domain.entity.{Role, User}
import itera.domain.valueobject.EntityId
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UserDTO(
  id: String,
  email: String,
  role: String,
  createdAt: Instant
)

object UserDTO {
  def fromEntity(u: User): UserDTO = UserDTO(
    id = u.id.show,
    email = u.email.show,
    role = u.role match {
      case Role.Admin     => "admin"
      case Role.Student   => "student"
      case Role.Counselor => "counselor"
    },
    createdAt = u.createdAt
  )

  implicit val encoder: Encoder[UserDTO] = deriveEncoder[UserDTO]
  implicit val decoder: Decoder[UserDTO] = deriveDecoder[UserDTO]
}
