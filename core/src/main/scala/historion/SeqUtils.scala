package historion

import scala.collection.GenSeq

object SeqUtils {

  implicit class SeqOps[A](seq: GenSeq[A]) {

    def countBy[B](f: A => B): Int = {
      seq.map(f).distinct.length
    }
  }
}
