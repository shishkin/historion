package historion

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.GenSeq
import scala.reflect.ClassTag

object SparkImplicits {

  implicit class SeqOps[T: ClassTag](xs: GenSeq[T]) {

    def toRdd()(implicit sc: SparkContext): RDD[T] =
      sc.parallelize(xs.seq)
  }

  implicit class FileStatsOps(xs: RDD[(Commit, FileStats)]) {

    def summary(): Summary = Summary(
      totalCommits = xs.map(_._1.id.value).distinct().count(),
      totalAuthors = xs.map(_._1.author.name).distinct().count(),
      totalFiles = xs.map(_._2.path).distinct().count(),
      fileChanges = xs.count())
  }
}
