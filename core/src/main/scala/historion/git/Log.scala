package historion.git

import org.eclipse.jgit.lib.{Constants, Repository}
import org.eclipse.jgit.revwalk.filter.{CommitTimeRevFilter, RevFilter}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}

import scala.collection.JavaConverters._
import scala.collection.parallel.ParSeq
import scala.concurrent.forkjoin.ForkJoinPool

object Log {

  def commits()(implicit repo: Repository): ParSeq[RevCommit] = {

    val revs = sequentialWalk(retainBody = false)
    val numBatches = ForkJoinPool.getCommonPoolParallelism - 1

    batches(from = revs.head, to = revs.last, count = numBatches).par
      .flatMap(f => sequentialWalk(filter = f))
  }

  private[this] def sequentialWalk(
    filter: RevFilter = RevFilter.ALL,
    retainBody: Boolean = true)
    (implicit repo: Repository)
  : Iterable[RevCommit] = {

    val walk = new RevWalk(repo)
    walk.setRetainBody(retainBody)
    walk.setRevFilter(filter)
    val head = repo.resolve(Constants.HEAD)
    walk.markStart(walk.lookupCommit(head))
    walk.asScala
  }

  private[this] def batches(from: RevCommit, to: RevCommit, count: Int)
  : IndexedSeq[RevFilter] = {

    val fromMillis = from.getCommitTime.toLong * 1000
    val toMillis = to.getCommitTime.toLong * 1000
    partition(fromMillis, toMillis, count).map { case (a, b) =>
      CommitTimeRevFilter.between(math.min(a, b), math.max(a, b))
    }
  }

  private[this] def partition(from: Long, to: Long, count: Int)
  : IndexedSeq[(Long, Long)] = {

    val size = math.ceil(math.abs(((to - 1) - from) / (count:Double))).toInt
    val one = math.signum(to - from)

    val lefts = Iterator.iterate(from)(_ + one * size).toStream
    val rights = lefts.drop(1).map(_ - one)
    val intervals = lefts.zip(rights).take(count).toVector
    intervals.init :+ intervals.last.copy(_2 = to)
  }
}
