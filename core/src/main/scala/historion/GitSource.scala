package historion

import java.io.File
import java.time.ZonedDateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.filter.{CommitTimeRevFilter, RevFilter}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.Function.tupled
import scala.collection.GenSeq
import scala.collection.JavaConverters._
import scala.collection.parallel.ParSeq
import scala.concurrent.forkjoin.ForkJoinPool

case class GitSource(repoDir: String) extends HistorySource {

  private[this] lazy val repo = Git.open(new File(repoDir)).getRepository

  def commits(): GenSeq[Commit] = for {
    rev <- parallelWalk()
    c = commit(rev)
    _ = rev.disposeBody()
  } yield c

  def fileStats(): GenSeq[(Commit, FileStats)] = for {
    rev <- parallelWalk()
    c = commit(rev)
    d <- patch(rev)
    stats = fileStats(d)
    _ = rev.disposeBody()
  } yield (c, stats)

  private[this] def sequentialWalk(
    filter: RevFilter = RevFilter.ALL,
    retainBody: Boolean = true)
  : Iterable[RevCommit] = {

    val walk = new RevWalk(repo)
    walk.setRetainBody(retainBody)
    walk.setRevFilter(filter)
    val head = repo.resolve(Constants.HEAD)
    walk.markStart(walk.lookupCommit(head))
    walk.asScala
  }

  private[this] def parallelWalk(): ParSeq[RevCommit] = for {
    filter <- revisionTimeFrames()
    rev <- sequentialWalk(filter = filter)
  } yield rev

  private[this] def revisionTimeFrames(): ParSeq[RevFilter] = {
    val revs = sequentialWalk(retainBody = false)
    val endMillis = revs.head.getCommitTime.toLong * 1000
    val beginMillis = revs.last.getCommitTime.toLong * 1000
    val intervalMillis =
      (endMillis - beginMillis) / (ForkJoinPool.getCommonPoolParallelism - 1)
    intervals(beginMillis, endMillis, intervalMillis)
      .map(tupled(CommitTimeRevFilter.between))
      .par
  }

  private[this] def intervals(start: Long, end: Long, size: Long): IndexedSeq[(Long, Long)] = {
    val steps = (start - 1 to end by size) :+ end
    steps.sliding(2)
      .toIndexedSeq
      .map { case Seq(a, b) => (a + 1, b) }
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
