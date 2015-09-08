package historion

import scala.collection.GenSeq

object Analyses {

  implicit class StatsOps(changes: GenSeq[(Commit, FileStats)]) {

    def summary(): Summary = Summary(
      totalCommits = changes.map(_._1.id).distinct.length,
      totalAuthors = changes.map(_._1.author).distinct.length,
      totalFiles = changes.map(_._2.path).distinct.length,
      fileChanges = changes.length)
  }
}
