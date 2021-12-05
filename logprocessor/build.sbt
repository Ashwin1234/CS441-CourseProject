import sbt._
import sbt.Keys._

lazy val root = (
  Project(id = "root", base = file("."))
    .enablePlugins(ScalafmtPlugin)
    .settings(
      name := "root",
      scalafmtOnCompile := true,
      skip in publish := true,
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      logProcessorPipeline,
      datamodel,
      akkaLogIngestor,
      sparkAggregator
    )
)

lazy val logProcessorPipeline = appModule("log-processor-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "log-processor"
  )

lazy val datamodel = appModule("datamodel")
  .enablePlugins(CloudflowLibraryPlugin)

val AkkaVersion = "2.6.15"
val AkkaHttpVersion = "10.2.4"

lazy val akkaLogIngestor= appModule("akka-log-ingestor")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"         %% "akka-http-spray-json"   % "10.1.12",
      "ch.qos.logback"            %  "logback-classic"        % "1.2.3",
      "org.scalatest"             %% "scalatest"              % "3.0.8"    % "test",

      "com.lightbend.akka"     %% "akka-stream-alpakka-file"  % "3.0.4",
      "com.typesafe.akka"      %% "akka-http-spray-json"      % "10.2.4",
      "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "3.0.4",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % AkkaHttpVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.122",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0"
    )
  )
  .dependsOn(datamodel)

lazy val sparkAggregator = appModule("spark-aggregator")
  .enablePlugins(CloudflowSparkPlugin)
  .settings(
    commonSettings,
    Test / fork := true,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % "1.2.3",
      "org.scalatest"  %% "scalatest"       % "3.0.8"  % "test"
    )
  )
  .dependsOn(datamodel)
  .dependsOn(akkaLogIngestor)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(
      name := moduleID
    )
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val commonSettings = Seq(
  organization := "com.lightbend.cloudflow",
  headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),
  scalaVersion := "2.12.11",
  javacOptions += "-Xlint:deprecation",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),

  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

)

dynverSeparator in ThisBuild := "-"