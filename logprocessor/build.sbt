import cloudflow.sbt.CloudflowAkkaPlugin


val AkkaVersion = "2.6.15"
val AkkaHttpVersion = "10.2.4"

lazy val logProcessor =  (project in file("."))
  .enablePlugins(CloudflowApplicationPlugin, CloudflowAkkaPlugin)
  .settings(
    scalaVersion := "2.13.3",
    runLocalConfigFile := Some("src/main/resources/local.conf"), //<1>
    //      runLocalLog4jConfigFile := Some("src/main/resources/log4j.xml"), //<2>
    name := "log-processor",
    //end::local-conf[]

    libraryDependencies ++= Seq(
      "com.lightbend.akka"     %% "akka-stream-alpakka-file"  % "3.0.4",
      "com.typesafe.akka"      %% "akka-http-spray-json"      % "10.2.4",
      "ch.qos.logback"         %  "logback-classic"           % "1.2.7",
      "com.typesafe.akka"      %% "akka-http-testkit"         % "10.2.6" % "test",
      "org.scalatest"          %% "scalatest"                 % "3.2.9"  % "test",
      "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "3.0.4",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % AkkaHttpVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.122",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0"
    )
  )
  //end::get-started[]
  .enablePlugins(ScalafmtPlugin)
  .settings(
    scalafmtOnCompile := true,

    organization := "com.lightbend.cloudflow",
    headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),

    crossScalaVersions := Vector(scalaVersion.value),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-target:jvm-1.8",
      "-Xlog-reflective-calls",
      "-Xlint",
      "-Ywarn-unused",
      "-deprecation",
      "-feature",
      "-language:_",
      "-unchecked"
    ),

    scalacOptions in (Compile, console) --= Seq("-Ywarn-unused"),
    scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
  )

dynverSeparator in ThisBuild := "-"