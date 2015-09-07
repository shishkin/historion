package historion

import java.io.File
import java.time.ZonedDateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.collection.JavaConverters._

case class GitSource(repoDir: String) extends HistorySource {

  private[this] lazy val repo = Git.open(new File(repoDir)).getRepository

  def commits(): Iterable[Commit] = for {
    rev <- walkFromHead()
    c = commit(rev)
    _ = rev.disposeBody()
  } yield c

  def fileStats(): Iterable[(Commit, FileStats)] = for {
    rev <- walkFromHead()
    c = commit(rev)
    d <- patch(rev)
    stats = fileStats(d)
    _ = rev.disposeBody()
  } yield (c, stats)

  private[this] def walkFromHead(): Iterable[RevCommit] = {
    val walk = new RevWalk(repo)
    val head = repo.resolve(Constants.HEAD)
    walk.markStart(walk.lookupCommit(head))
    walk.asScala
  }

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

  private[this] def patch(rev: RevCommit): Seq[FileHeader] = {

    val revTree = rev.getTree
    val parentTree = rev.getParents
      .headOption
      .map(_.getTree)
      .orNull

    val diff = new DiffFormatter(DisabledOutputStream.INSTANCE)
    diff.setRepository(repo)
    diff.setContext(0)
    diff.scan(parentTree, revTree).asScala
      .map(diff.toFileHeader)
  }

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
