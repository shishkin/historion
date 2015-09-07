package historion

import org.scalatest.{Matchers, FunSuite}

class GitSourceTest extends FunSuite with Matchers {

  def currentRepoLog = GitSource.log(".")

  test("initial commit is the last one") {

    val commit = currentRepoLog.last
    commit.message should startWith("initial commit")
  }

  test("timezone is correct") {

    val commit = currentRepoLog.last
    commit.timestamp.getZone.getId should be ("GMT+08:00")
  }

  test("sha1 is valid") {

    val commit = currentRepoLog.last
    commit.id.value should fullyMatch regex """[0-9a-f]{40}""".r
  }


  test("file stats") {

    val commit = currentRepoLog.last
    val diff = GitSource.fileStats(".").filter(_._1 == commit).map(_._2)
    val files = diff.map(_.path)

    files should contain theSameElementsAs List(
      ".gitignore", "README.md", "build.sbt", "project/build.properties",
      "project/plugins.sbt")

    val totalLines = diff.foldLeft(0) {
      (total, d) => total + d.linesAdded - d.linesRemoved
    }

    totalLines should be(15)
  }
}
