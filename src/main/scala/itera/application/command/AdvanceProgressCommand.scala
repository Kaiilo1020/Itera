package itera.application.command

import itera.domain.valueobject.EntityId

final case class AdvanceProgressCommand(
  userId: EntityId,
  act: Int,
  stage: Int
)
