import scala.sys.process._

name := "SpinalHdlTechTalk"
version := "1.0"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.github.spinalhdl" % "spinalhdl-core_2.12" % "1.3.6",
  "com.github.spinalhdl" % "spinalhdl-lib_2.12" % "1.3.6",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

fork := true
testForkedParallel := true

enablePlugins(TutPlugin)

lazy val presentation = taskKey[Unit]("Generates the presentation")
presentation := {
  tut.value
  "pandoc -t revealjs -s -o presentation.html target/scala-2.12/tut/Talk.md -V revealjs-url=https://revealjs.com -V theme=white" !
}
