package itera.domain.error

sealed trait DomainError extends Product with Serializable {
  def message: String
}

object DomainError {
  final case class NotFound(resource: String, id: String) extends DomainError {
    def message: String = s"$resource not found: $id"
  }
  final case class AlreadyExists(resource: String, field: String) extends DomainError {
    def message: String = s"$resource already exists with $field"
  }
  final case class ValidationError(msg: String) extends DomainError {
    def message: String = msg
  }
  final case class AuthenticationError(msg: String) extends DomainError {
    def message: String = msg
  }
  final case class AuthorizationError(msg: String) extends DomainError {
    def message: String = msg
  }
  final case class InternalError(msg: String) extends DomainError {
    def message: String = msg
  }
}
