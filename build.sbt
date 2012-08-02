name := "telepost"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.0",
	"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.0",
	"joda-time" % "joda-time" % "1.6", 
    "net.databinder" % "dispatch-twitter_2.9.0-1" % "0.8.3" % "compile->default",
	"javax.mail" % "mail" % "1.4"
)
	
mainClass := Some("Main")

scalaVersion := "2.9.2"

