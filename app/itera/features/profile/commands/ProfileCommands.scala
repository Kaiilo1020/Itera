package itera.features.profile.commands

import java.util.UUID
import itera.features.profile.domain.Skill

final case class InitializeProfileCommand(
  userId: UUID,
  names: String,
  surnames: String,
  institutionId: Option[UUID],
  cycle: Int,
  skills: List[Skill]
)

final case class UpdateProfileCommand(
  userId: UUID,
  names: Option[String],
  surnames: Option[String],
  institutionId: Option[UUID],
  cycle: Option[Int],
  skills: Option[List[Skill]],
  photo: Option[String],
  academicGoal: Option[String]
)
