package itera.features.profile.domain

import java.util.UUID

final case class Student(
  id: UUID,
  userId: UUID,
  institutionId: Option[UUID],
  names: String,
  surnames: String,
  cycle: Int,
  academicGoal: String = "General"
)

object Student {
  def create(
    userId: UUID,
    names: String,
    surnames: String,
    institutionId: Option[UUID] = None,
    cycle: Int = 1,
    academicGoal: String = "General"
  ): Student = {
    Student(
      id = UUID.randomUUID(),
      userId = userId,
      institutionId = institutionId,
      names = names,
      surnames = surnames,
      cycle = cycle,
      academicGoal = academicGoal
    )
  }
}
