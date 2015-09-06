package historion

import scala.collection.JavaConverters._

import java.io.File
import java.time.ZonedDateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit

trait GitSource {

  private[this] def commit(c: RevCommit): Commit =
    Commit(
      Sha1(c.getId.name()),
      authoredTimestamp(c),
      Author(c.getAuthorIdent.getName),
      c.getFullMessage)

  private[this] def authoredTimestamp(c: RevCommit): ZonedDateTime =
    ZonedDateTime.ofInstant(
      c.getAuthorIdent.getWhen.toInstant,
      c.getAuthorIdent.getTimeZone.toZoneId)

  def log(repoDir: String): Stream[Commit] = {
    val git = Git.open(new File(repoDir))
    val command = git.log().all()
    command
      .call().asScala
      .map(commit)
      .toStream
  }
}

object GitSource extends GitSource
