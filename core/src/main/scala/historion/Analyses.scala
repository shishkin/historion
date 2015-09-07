package historion

object Analyses {

  implicit class StatsOps(val changes: Stream[(Commit, FileStats)]) extends AnyVal {

    def summary(): Summary = Summary(
      totalCommits = changes.map(_._1.id.value).distinct.length,
      totalAuthors = changes.map(_._1.author.name).distinct.length,
      totalFiles = changes.map(_._2.path).distinct.length,
      fileChanges = changes.length)
  }
}
