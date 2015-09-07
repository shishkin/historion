package historion

import org.scalatest.{Matchers, FunSuite}

class GitSourceTest extends FunSuite with Matchers {

  def currentRepo = GitSource(".")

  def initialCommit = currentRepo.commits().last

  test("initial commit is the last one") {

    initialCommit.message should startWith("initial commit")
  }


  test("timezone is correct") {

    initialCommit.timestamp.getZone.getId should be ("GMT+08:00")
  }

  test("sha1 is valid") {

    initialCommit.id.value should fullyMatch regex """[0-9a-f]{40}""".r
  }


  test("file stats") {

    val commit = initialCommit
    val diff = currentRepo.fileStats().filter(_._1 == commit).map(_._2)
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
