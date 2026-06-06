package itera.application.query.dto

import java.time.Instant
import itera.domain.entity.{Progress, ProgressStatus}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ProgressDTO(
  act: Int,
  stage: Int,
  status: String,
  completedAt: Option[Instant]
)

object ProgressDTO {
  def fromEntity(p: Progress): ProgressDTO = ProgressDTO(
    act = p.act,
    stage = p.stage,
    status = p.status match {
      case ProgressStatus.NotStarted => "not_started"
      case ProgressStatus.InProgress => "in_progress"
      case ProgressStatus.Completed  => "completed"
    },
    completedAt = p.completedAt
  )

  implicit val encoder: Encoder[ProgressDTO] = deriveEncoder[ProgressDTO]
  implicit val decoder: Decoder[ProgressDTO] = deriveDecoder[ProgressDTO]
}
