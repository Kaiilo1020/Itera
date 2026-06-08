package itera.features.progress.commands

import java.util.UUID

final case class SubmitEvidenceCommand(
  userId: UUID,
  nodeId: String,
  evidenceUrl: String
)

final case class ApproveNodeCommand(
  userId: UUID,
  nodeId: String,
  grade: BigDecimal
)
