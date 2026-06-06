package itera.infrastructure.persistence

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.implicits._

object Migrations {

  def run[F[_]: MonadCancelThrow](xa: Transactor[F]): F[Unit] = {
    val ddl = List(
      sql"""
        CREATE TABLE IF NOT EXISTS users (
          id UUID PRIMARY KEY,
          email VARCHAR(255) UNIQUE NOT NULL,
          password_hash VARCHAR(255) NOT NULL,
          role VARCHAR(50) NOT NULL,
          created_at TIMESTAMP WITH TIME ZONE NOT NULL
        )
      """.update.run,
      sql"""
        CREATE TABLE IF NOT EXISTS profiles (
          user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
          name VARCHAR(255) NOT NULL,
          interests TEXT[] NOT NULL,
          education_level VARCHAR(50) NOT NULL
        )
      """.update.run,
      sql"""
        CREATE TABLE IF NOT EXISTS progress (
          user_id UUID REFERENCES users(id) ON DELETE CASCADE,
          act INT NOT NULL,
          stage INT NOT NULL,
          status VARCHAR(50) NOT NULL,
          completed_at TIMESTAMP WITH TIME ZONE,
          PRIMARY KEY (user_id, act, stage)
        )
      """.update.run
    )
    ddl.traverse_(_.transact(xa)).void
  }
}
