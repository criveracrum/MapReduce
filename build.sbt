

lazy val root = (project in file(".")).
settings (
  name := "MapReduce2",
  version := "1.0",
  scalaVersion := "2.13.5",
  scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation"),
  resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.14",
//  libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.6.14",
  libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.14",
  libraryDependencies +="com.typesafe.akka" %% "akka-cluster" % "2.6.14",
  libraryDependencies +="com.typesafe.akka" %% "akka-cluster-tools" % "2.6.14"
)
