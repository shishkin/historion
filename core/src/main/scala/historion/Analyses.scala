package historion

import scala.collection.GenSeq

import historion.SeqUtils._

object Analyses {

  implicit class StatsOps(changes: GenSeq[(Commit, FileStats)]) {

    def summary(): Summary = Summary(
      totalCommits = changes.countBy(_._1.id),
      totalAuthors = changes.countBy(_._1.author),
      totalFiles = changes.countBy(_._2.path),
      fileChanges = changes.length)

    def fileSummary(): GenSeq[(String, Summary)] = changes
      .map { case (commit, change) => (change.path, commit.author) }
      .groupBy(_._1)
      .mapValues(
        changes => Summary(
          totalCommits = changes.length,
          totalAuthors = changes.countBy(_._2),
          totalFiles = 1,
          fileChanges = changes.length))
      .toIndexedSeq
      .sortBy(-_._2.totalCommits)
  }
}
