name in ThisBuild := "historion"

version in ThisBuild := "0.1.0"

scalaVersion in ThisBuild := "2.11.7"

crossPaths in ThisBuild := false

lazy val core = project.settings(

  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.12",
    "org.slf4j" % "slf4j-simple" % "1.7.12",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"),

  initialCommands in console :=
    """
      |import historion._, historion.git._,
      |  Analyses._
    """.stripMargin
)

lazy val spark = project.dependsOn(core).settings(

  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "1.4.1"),

  mainClass in Compile := Some("historion.Main"),

  initialCommands in console :=
    """
      |import org.apache.spark._, historion._, historion.git._, Main._,
      |  SparkImplicits._
    """.stripMargin,

  fork in run := true
)
