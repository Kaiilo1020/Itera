package itera.features.profile.domain

import java.util.UUID
import io.circe.Json

final case class Skill(name: String, level: Int)

final case class Profile(
  studentId: UUID,
  photo: Option[String],
  experience: Int,
  preferences: Option[Json],
  skills: List[Skill],
  badges: Option[Json]
)

object Profile {
  def empty(studentId: UUID): Profile = {
    Profile(
      studentId = studentId,
      photo = None,
      experience = 0,
      preferences = None,
      skills = Nil,
      badges = None
    )
  }
}
