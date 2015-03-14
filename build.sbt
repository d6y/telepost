name := "telepost"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "javax.mail" % "mail" % "1.4",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "org.apache.sanselan" % "sanselan" % "0.97-incubator"
)

// http://www.scala-sbt.org/0.13/docs/Forking.html

fork := true

mainClass := Some("Main")

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

