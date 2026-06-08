package itera.features.profile.domain

import java.util.UUID

final case class Student(
  id: UUID,
  userId: UUID,
  institutionId: Option[UUID],
  names: String,
  surnames: String,
  cycle: Int
)

object Student {
  def create(
    userId: UUID,
    names: String,
    surnames: String,
    institutionId: Option[UUID] = None,
    cycle: Int = 1
  ): Student = {
    Student(
      id = UUID.randomUUID(),
      userId = userId,
      institutionId = institutionId,
      names = names,
      surnames = surnames,
      cycle = cycle
    )
  }
}
