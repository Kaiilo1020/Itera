package itera.domain.entity

sealed trait ProgressStatus extends Product with Serializable

object ProgressStatus {
  case object NotStarted extends ProgressStatus
  case object InProgress extends ProgressStatus
  case object Completed  extends ProgressStatus
}
