package itera.presentation.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvanceProgressRequest(
  act: Int,
  stage: Int
)

object AdvanceProgressRequest {
  implicit val decoder: Decoder[AdvanceProgressRequest] = deriveDecoder[AdvanceProgressRequest]
  implicit val encoder: Encoder[AdvanceProgressRequest] = deriveEncoder[AdvanceProgressRequest]
}
