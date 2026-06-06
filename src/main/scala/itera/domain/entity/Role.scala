package itera.domain.entity

sealed trait Role extends Product with Serializable

object Role {
  case object Admin     extends Role
  case object Student   extends Role
  case object Counselor extends Role
}
