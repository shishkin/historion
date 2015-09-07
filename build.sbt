name in ThisBuild := "historion"

version in ThisBuild := "0.1.0"

scalaVersion in ThisBuild := "2.11.7"

crossPaths in ThisBuild := false

lazy val core = project.settings(

  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.12",
    "org.slf4j" % "slf4j-simple" % "1.7.12",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test")
)
