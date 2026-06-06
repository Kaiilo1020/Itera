package itera.domain.entity

sealed trait EducationLevel extends Product with Serializable

object EducationLevel {
  case object HighSchool extends EducationLevel
  case object Bachelor   extends EducationLevel
  case object Master     extends EducationLevel
  case object Doctorate  extends EducationLevel
  case object Other      extends EducationLevel
}
