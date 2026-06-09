package itera.features.profile.infrastructure

import cats.effect.Async
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres.circe.jsonb.implicits._
import itera.features.profile.domain.{Profile, Skill, Student, StudentRepository}
import java.util.UUID
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._

class DoobieStudentRepository[F[_]: Async](xa: Transactor[F]) extends StudentRepository[F] {

  // Custom mapping for List[Skill] by manually handling the string/JSON conversion
  // This avoids the complex implicit resolution for jsonb in Doobie if it's failing
  implicit val skillListMeta: Meta[List[Skill]] = Meta[String].imap(
    str => io.circe.parser.decode[List[Skill]](str).getOrElse(Nil)
  )(skills => skills.asJson.noSpaces)

  override def findByUserId(userId: UUID): F[Option[Student]] =
    sql"SELECT id, user_id, institution_id, names, surnames, cycle, academic_goal FROM students WHERE user_id = $userId"
      .query[Student]
      .option
      .transact(xa)

  override def findProfileByStudentId(studentId: UUID): F[Option[Profile]] =
    sql"SELECT student_id, photo, experience, preferences, skills, badges FROM profiles WHERE student_id = $studentId"
      .query[Profile]
      .option
      .transact(xa)

  override def saveStudent(student: Student): F[Unit] =
    sql"""
      INSERT INTO students (id, user_id, institution_id, names, surnames, cycle, academic_goal)
      VALUES (${student.id}, ${student.userId}, ${student.institutionId}, ${student.names}, ${student.surnames}, ${student.cycle}, ${student.academicGoal})
    """.update.run.transact(xa).void

  override def saveProfile(profile: Profile): F[Unit] =
    sql"""
      INSERT INTO profiles (student_id, photo, experience, preferences, skills, badges)
      VALUES (
        ${profile.studentId}, 
        ${profile.photo}, 
        ${profile.experience}, 
        ${profile.preferences.asJson}::jsonb, 
        ${profile.skills.asJson}::jsonb, 
        ${profile.badges.asJson}::jsonb
      )
    """.update.run.transact(xa).void

  override def updateStudent(student: Student): F[Unit] =
    sql"""
      UPDATE students 
      SET institution_id = ${student.institutionId}, names = ${student.names}, surnames = ${student.surnames}, cycle = ${student.cycle}, academic_goal = ${student.academicGoal}
      WHERE id = ${student.id}
    """.update.run.transact(xa).void

  override def updateProfile(profile: Profile): F[Unit] =
    sql"""
      UPDATE profiles
      SET 
        photo = ${profile.photo}, 
        experience = ${profile.experience}, 
        preferences = ${profile.preferences.asJson}::jsonb, 
        skills = ${profile.skills.asJson}::jsonb, 
        badges = ${profile.badges.asJson}::jsonb
      WHERE student_id = ${profile.studentId}
    """.update.run.transact(xa).void
}
