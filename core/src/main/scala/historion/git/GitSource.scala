package historion.git

import java.io.File
import java.time.ZonedDateTime

import historion._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit

import scala.collection.JavaConverters._
import scala.collection.GenSeq

case class GitSource(repoDir: String) extends HistorySource {

  private[this] implicit lazy val repo =
    Git.open(new File(repoDir)).getRepository

  def commits(): GenSeq[Commit] = for {
    rev <- Log.commits()
    c = commit(rev)
    _ = rev.disposeBody()
  } yield c

  def fileStats(): GenSeq[(Commit, FileStats)] = for {
    rev <- Log.commits()
    c = commit(rev)
    d <- Diff.patch(rev)
    stats = fileStats(d)
    _ = rev.disposeBody()
  } yield (c, stats)

  private[this] def commit(c: RevCommit): Commit =
    Commit(
      Sha1(c.getId.name()),
      authoredTimestamp(c),
      Author(c.getAuthorIdent.getName),
      c.getShortMessage)

  private[this] def authoredTimestamp(c: RevCommit): ZonedDateTime =
    ZonedDateTime.ofInstant(
      c.getAuthorIdent.getWhen.toInstant,
      c.getAuthorIdent.getTimeZone.toZoneId)

  private[this] def fileStats(file: FileHeader): FileStats = {
    file.getHunks.asScala
      .flatMap(_.toEditList.asScala)
      .foldLeft(FileStats(file.getNewPath)) {

      (stats, edit) => stats.copy(
        linesAdded = stats.linesAdded + edit.getLengthB,
        linesRemoved = stats.linesRemoved + edit.getLengthA)
    }
  }
}
