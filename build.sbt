name := "DSV"

version := "1.0"

scalaVersion := "2.12.4"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++=
  "com.typesafe.akka" %% "akka-actor" % "2.5.6" ::
    "com.typesafe.akka" %% "akka-remote" % "2.5.6" ::
    Nil