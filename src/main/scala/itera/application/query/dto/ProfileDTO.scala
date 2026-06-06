package itera.application.query.dto

import itera.domain.entity.{EducationLevel, Profile}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ProfileDTO(
  userId: String,
  name: String,
  interests: List[String],
  educationLevel: String
)

object ProfileDTO {
  def fromEntity(p: Profile): ProfileDTO = ProfileDTO(
    userId = p.userId.show,
    name = p.name,
    interests = p.interests,
    educationLevel = p.educationLevel match {
      case EducationLevel.HighSchool => "high_school"
      case EducationLevel.Bachelor   => "bachelor"
      case EducationLevel.Master     => "master"
      case EducationLevel.Doctorate  => "doctorate"
      case EducationLevel.Other      => "other"
    }
  )

  implicit val encoder: Encoder[ProfileDTO] = deriveEncoder[ProfileDTO]
  implicit val decoder: Decoder[ProfileDTO] = deriveDecoder[ProfileDTO]
}
