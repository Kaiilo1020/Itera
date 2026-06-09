package itera.features.profile.queries

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.profile.domain.StudentRepository
import itera.features.goals.domain.GoalRepository
import itera.features.progress.domain.ProgressRepository
import itera.shared.domain.DomainError
import itera.shared.infrastructure.{LogicClient, IAClient}
import java.util.UUID

class ProfileQueryHandlers[F[_]: Async](
  repo: StudentRepository[F],
  goalRepo: GoalRepository[F],
  progressRepo: ProgressRepository[F],
  logicClient: LogicClient,
  iaClient: IAClient
) {

  def getProfile(userId: UUID): F[Either[DomainError, StudentProfileView]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import itera.features.profile.domain.{Student, Profile}
    
    println(s"🔍 [DEBUG] Solicitando perfil para userId: $userId")

    val result: EitherT[F, DomainError, StudentProfileView] = for {
      // 1. Ensure Student record exists
      maybeStudent <- EitherT.right[DomainError](repo.findByUserId(userId))
      student <- maybeStudent match {
        case Some(s) => EitherT.rightT[F, DomainError](s)
        case None => 
          val newStudent = Student.create(userId, "Usuario", "Nuevo")
          EitherT.right[DomainError](repo.saveStudent(newStudent)).as(newStudent)
      }
      
      // 2. Ensure Profile record exists
      maybeProfile <- EitherT.right[DomainError](repo.findProfileByStudentId(student.id))
      profile <- maybeProfile match {
        case Some(p) => EitherT.rightT[F, DomainError](p)
        case None => 
          val newProfile = Profile.empty(student.id)
          EitherT.right[DomainError](repo.saveProfile(newProfile)).as(newProfile)
      }

      goal    <- EitherT.right[DomainError](goalRepo.findByStudentId(student.id))
      
      // Get approved nodes from progress table (mocked for now)
      approvedNodes = Nil 
      
      hours = goal.map(_.hoursPerWeek).getOrElse(20)
      pace = "normal" 
      
      // Call logic engine with resilience
      maybeLogicData <- EitherT.right[DomainError](Async[F].fromFuture(Async[F].delay {
        logicClient.getRoadmap(student.id, approvedNodes, profile.skills.map(_.name), hours, pace)
      }))

      // Call IA microservice for match score
      maybeMatchData <- EitherT.right[DomainError](Async[F].fromFuture(Async[F].delay {
        iaClient.getMatchScore(student.id, profile.skills.map(_.name), student.academicGoal)
      }))
      
    } yield StudentProfileView(
      id = student.id,
      userId = student.userId,
      names = student.names,
      surnames = student.surnames,
      cycle = student.cycle,
      academicGoal = student.academicGoal,
      institutionId = student.institutionId,
      photo = profile.photo,
      experience = profile.experience,
      skills = profile.skills,
      // Convert Circe Json to Play JSValue for the view
      badges = profile.badges.map(json => play.api.libs.json.Json.parse(json.noSpaces)),
      roadmap = maybeLogicData.flatMap(json => (json \ "roadmap").asOpt[play.api.libs.json.JsValue]),
      projection = maybeLogicData.flatMap(json => (json \ "projection").asOpt[play.api.libs.json.JsValue]),
      recommendations = maybeLogicData.flatMap(json => (json \ "recommendations").asOpt[play.api.libs.json.JsValue]),
      matchScore = maybeMatchData
    )

    result.value
  }
}
