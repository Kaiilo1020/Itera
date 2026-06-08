package itera.features.admin.infrastructure

import cats.effect.Async
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import itera.features.admin.domain.{CatalogRepository, Institution, ProgressState}
import java.util.UUID

class DoobieCatalogRepository[F[_]: Async](xa: Transactor[F]) extends CatalogRepository[F] {

  override def listInstitutions(): F[List[Institution]] =
    sql"SELECT id, acronym, full_name, status FROM institutions"
      .query[Institution]
      .to[List]
      .transact(xa)

  override def listProgressStates(): F[List[ProgressState]] =
    sql"SELECT id, name FROM progress_states"
      .query[ProgressState]
      .to[List]
      .transact(xa)

  override def findInstitutionById(id: UUID): F[Option[Institution]] =
    sql"SELECT id, acronym, full_name, status FROM institutions WHERE id = $id"
      .query[Institution]
      .option
      .transact(xa)

  override def saveInstitution(institution: Institution): F[Unit] =
    sql"""
      INSERT INTO institutions (id, acronym, full_name, status)
      VALUES (${institution.id}, ${institution.acronym}, ${institution.fullName}, ${institution.status})
    """.update.run.transact(xa).void

  override def updateInstitution(institution: Institution): F[Unit] =
    sql"""
      UPDATE institutions
      SET acronym = ${institution.acronym}, full_name = ${institution.fullName}, status = ${institution.status}
      WHERE id = ${institution.id}
    """.update.run.transact(xa).void

  override def deleteInstitution(id: UUID): F[Unit] =
    sql"DELETE FROM institutions WHERE id = $id"
      .update.run.transact(xa).void
}
