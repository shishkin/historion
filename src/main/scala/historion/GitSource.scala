package historion

import org.eclipse.jgit.lib.Constants

import scala.collection.JavaConverters._

import java.io.File
import java.time.ZonedDateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}

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
}

object GitSource extends GitSource
