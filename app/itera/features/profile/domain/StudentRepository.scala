package itera.features.profile.domain

import java.util.UUID

trait StudentRepository[F[_]] {
  def findByUserId(userId: UUID): F[Option[Student]]
  def findProfileByStudentId(studentId: UUID): F[Option[Profile]]
  def saveStudent(student: Student): F[Unit]
  def saveProfile(profile: Profile): F[Unit]
  def updateStudent(student: Student): F[Unit]
  def updateProfile(profile: Profile): F[Unit]
}
