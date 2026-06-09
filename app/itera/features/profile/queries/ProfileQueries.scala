package itera.features.profile.queries

import java.util.UUID
import itera.features.profile.domain.Skill

final case class StudentProfileView(
  id: UUID,
  userId: UUID,
  names: String,
  surnames: String,
  cycle: Int,
  academicGoal: String,
  institutionId: Option[UUID],
  photo: Option[String],
  experience: Int,
  skills: List[Skill],
  badges: Option[play.api.libs.json.JsValue] = None,
  roadmap: Option[play.api.libs.json.JsValue] = None,
  projection: Option[play.api.libs.json.JsValue] = None,
  recommendations: Option[play.api.libs.json.JsValue] = None,
  matchScore: Option[play.api.libs.json.JsValue] = None
)
