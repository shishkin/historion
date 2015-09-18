package historion

import scala.collection.GenSeq

object Analyses {

  implicit class StatsOps(changes: GenSeq[(Commit, FileStats)]) {

    def summary(): Summary = Summary(
      totalCommits = changes.map(_._1.id).distinct.length,
      totalAuthors = changes.map(_._1.author).distinct.length,
      totalFiles = changes.map(_._2.path).distinct.length,
      fileChanges = changes.length)

    def fileSummary(): GenSeq[(String, Summary)] = changes
      .map { case (commit, change) => (change.path, commit.author) }
      .groupBy(_._1)
      .mapValues(
        changes => Summary(
          totalCommits = changes.length,
          totalAuthors = changes.map(_._2).distinct.length,
          totalFiles = 1,
          fileChanges = changes.length))
      .toIndexedSeq
      .sortBy(-_._2.totalCommits)
  }
}
