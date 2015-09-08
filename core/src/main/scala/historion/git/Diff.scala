package historion.git

import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.collection.JavaConverters._

object Diff {

  def patch(rev: RevCommit)
    (implicit repo: Repository)
  : IndexedSeq[FileHeader] = {

    val revTree = rev.getTree
    val parentTree = rev.getParents
      .headOption
      .map(_.getTree)
      .orNull

    val diff = new DiffFormatter(DisabledOutputStream.INSTANCE)
    diff.setRepository(repo)
    diff.setContext(0)
    diff.scan(parentTree, revTree).asScala
      .map(diff.toFileHeader)
      .toIndexedSeq
  }
}
