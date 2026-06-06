package itera.domain.entity

import itera.domain.valueobject.EntityId

final case class Profile(
  userId: EntityId,
  name: String,
  interests: List[String],
  educationLevel: EducationLevel
)
