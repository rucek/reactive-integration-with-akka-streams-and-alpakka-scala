name := "reactive-integration-with-akka-streams-and-alpakka-scala"

version := "0.1"

scalaVersion := "2.13.1"

val alpakkaVersion = "1.1.2"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % alpakkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % alpakkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % alpakkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.10"
)