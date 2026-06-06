package itera.application.command

import itera.domain.entity.EducationLevel
import itera.domain.valueobject.EntityId

final case class UpdateProfileCommand(
  userId: EntityId,
  name: String,
  interests: List[String],
  educationLevel: EducationLevel
)
