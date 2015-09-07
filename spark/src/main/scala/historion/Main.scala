package historion

import org.apache.spark.SparkContext

object Main {

  import SparkImplicits._

  implicit lazy val sc = new SparkContext("local[4]", "historion")

  def main(args: Array[String]): Unit = {

    val git = GitSource("..")
    val stats = git.fileStats().toRdd()
    val summary = stats.summary()
    println(summary)
  }
}
