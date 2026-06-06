package itera.presentation.dto

import itera.domain.entity.EducationLevel
import itera.domain.valueobject.EntityId
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateProfileRequest(
  name: String,
  interests: List[String],
  educationLevel: String
) {
  def toDomain(userId: EntityId): Either[String, (String, List[String], EducationLevel)] = {
    val edu = educationLevel.toLowerCase match {
      case "high_school" => Right(EducationLevel.HighSchool)
      case "bachelor"    => Right(EducationLevel.Bachelor)
      case "master"      => Right(EducationLevel.Master)
      case "doctorate"   => Right(EducationLevel.Doctorate)
      case "other"       => Right(EducationLevel.Other)
      case other         => Left(s"Invalid education level: $other")
    }
    edu.map(e => (name, interests, e))
  }
}

object UpdateProfileRequest {
  implicit val decoder: Decoder[UpdateProfileRequest] = deriveDecoder[UpdateProfileRequest]
  implicit val encoder: Encoder[UpdateProfileRequest] = deriveEncoder[UpdateProfileRequest]
}
