package itera.features.profile.queries

import cats.effect.Async
import cats.data.EitherT
import cats.syntax.all._
import itera.features.profile.domain.StudentRepository
import itera.features.goals.domain.GoalRepository
import itera.features.progress.domain.ProgressRepository
import itera.shared.domain.DomainError
import itera.shared.infrastructure.LogicClient
import java.util.UUID

class ProfileQueryHandlers[F[_]: Async](
  repo: StudentRepository[F],
  goalRepo: GoalRepository[F],
  progressRepo: ProgressRepository[F],
  logicClient: LogicClient
) {

  def getProfile(userId: UUID): F[Either[DomainError, StudentProfileView]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val result: EitherT[F, DomainError, StudentProfileView] = for {
      student <- EitherT.fromOptionF(repo.findByUserId(userId), DomainError("Student profile not found"))
      profile <- EitherT.fromOptionF(repo.findProfileByStudentId(student.id), DomainError("Academic profile not found"))
      goal    <- EitherT.right[DomainError](goalRepo.findByStudentId(student.id))
      
      // Get approved nodes from progress table (mocked for now)
      approvedNodes = Nil 
      
      hours = goal.map(_.hoursPerWeek).getOrElse(20)
      pace = "normal" 
      
      // Call logic engine with resilience
      maybeData <- EitherT.right[DomainError](Async[F].fromFuture(Async[F].delay {
        logicClient.getRoadmap(student.id, approvedNodes, profile.skills.map(_.name), hours, pace)
      }))
      
    } yield StudentProfileView(
      id = student.id,
      userId = student.userId,
      names = student.names,
      surnames = student.surnames,
      cycle = student.cycle,
      institutionId = student.institutionId,
      photo = profile.photo,
      experience = profile.experience,
      skills = profile.skills,
      // Convert Circe Json to Play JSValue for the view
      badges = profile.badges.map(json => play.api.libs.json.Json.parse(json.noSpaces)),
      roadmap = maybeData.flatMap(json => (json \ "roadmap").asOpt[play.api.libs.json.JsValue]),
      projection = maybeData.flatMap(json => (json \ "projection").asOpt[play.api.libs.json.JsValue]),
      recommendations = maybeData.flatMap(json => (json \ "recommendations").asOpt[play.api.libs.json.JsValue])
    )

    result.value
  }
}
