package historion

import org.scalatest.{Matchers, FunSuite}

class GitSourceTest extends FunSuite with Matchers {

  def currentRepoLog = GitSource.log(".")

  test("initial commit is the last one") {

    val commit = currentRepoLog.last
    commit.message should startWith("initial commit")
  }

  test("timezone is correct") {

    val commit = currentRepoLog.head
    commit.timestamp.getZone.getId should be ("GMT+08:00")
  }

  test("sha1 is valid") {

    val commit = currentRepoLog.head
    commit.id.value should fullyMatch regex """[0-9a-f]{40}""".r
  }
}
