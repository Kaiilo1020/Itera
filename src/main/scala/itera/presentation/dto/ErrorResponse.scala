package itera.presentation.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ErrorResponse(
  error: String,
  message: String
)

object ErrorResponse {
  implicit val decoder: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]
  implicit val encoder: Encoder[ErrorResponse] = deriveEncoder[ErrorResponse]
}
