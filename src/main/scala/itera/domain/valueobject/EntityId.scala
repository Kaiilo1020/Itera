package itera.domain.valueobject

import java.util.UUID
import cats.effect.Sync
import cats.syntax.all._

final case class EntityId(value: UUID) extends AnyVal {
  def show: String = value.toString
}

object EntityId {
  def generate[F[_]: Sync]: F[EntityId] =
    Sync[F].delay(EntityId(UUID.randomUUID()))

  def fromString(s: String): Either[String, EntityId] =
    Either.catchNonFatal(UUID.fromString(s)).leftMap(_ => s"Invalid UUID: $s").map(EntityId(_))
}
