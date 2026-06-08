package itera.features.admin.domain

import java.util.UUID

final case class Institution(
  id: UUID,
  acronym: String,
  fullName: String,
  status: String
)

final case class ProgressState(
  id: UUID,
  name: String
)

trait CatalogRepository[F[_]] {
  def listInstitutions(): F[List[Institution]]
  def listProgressStates(): F[List[ProgressState]]
  def findInstitutionById(id: UUID): F[Option[Institution]]
  
  // CRUD Operations for RF-17
  def saveInstitution(institution: Institution): F[Unit]
  def updateInstitution(institution: Institution): F[Unit]
  def deleteInstitution(id: UUID): F[Unit]
}
