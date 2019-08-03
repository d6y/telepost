name := "telepost"

version := "1.0.1"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"        %% "scala-xml"        % "1.2.0",
  "com.github.scala-incubator.io" %% "scala-io-core"    % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file"    % "0.4.3",
  "javax.mail"                    % "mail"              % "1.4.7",
  "com.amazonaws"                 % "aws-java-sdk-s3"   % "1.11.603",
  "com.amazonaws"                 % "aws-java-sdk-core" % "1.11.603",
  "org.im4java"                   % "im4java"           % "1.4.0"
)

fork := true

mainClass := Some("Main")

scalaVersion := "2.11.12"

scalacOptions ++= Seq("-Ywarn-value-discard", "-unchecked", "-deprecation", "-feature")
