package historion

import java.io.File
import java.time.ZonedDateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.{Constants, Repository}
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.collection.JavaConverters._

trait GitSource {

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

  def log(repoDir: String): Stream[Commit] = {
    val repo = Git.open(new File(repoDir))
      .getRepository
    val walk = new RevWalk(repo)
    val head = repo.resolve(Constants.HEAD)
    walk.markStart(walk.lookupCommit(head))
    walk.asScala
      .toStream
      .map(commit)
  }

  private[this] def diff(repo: Repository, rev: RevCommit): Seq[FileHeader] = {

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

  def fileStats(repoDir: String): Stream[(Commit, FileStats)] = {
    val repo = Git.open(new File(repoDir))
      .getRepository
    val walk = new RevWalk(repo)
    val head = repo.resolve(Constants.HEAD)
    walk.markStart(walk.lookupCommit(head))
    for {
      rev <- walk.asScala.toStream
      c = commit(rev)
      d <- diff(repo, rev)
      stats = fileStats(d)
    } yield (c, stats)
  }
}

object GitSource extends GitSource
