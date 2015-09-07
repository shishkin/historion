package historion

import java.time.ZonedDateTime

case class Sha1(value: String)

case class Author(name: String)

case class Commit(
  id: Sha1,
  timestamp: ZonedDateTime,
  author: Author,
  message: String)

case class FileStats(
  path: String,
  linesAdded: Int = 0,
  linesRemoved: Int = 0)

case class Summary(
  totalCommits: Long,
  totalFiles: Long,
  totalAuthors: Long,
  fileChanges: Long)

trait HistorySource {

  def commits(): Stream[Commit]

  def fileStats(): Stream[(Commit, FileStats)]
}