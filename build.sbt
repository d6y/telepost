name := "telepost"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"        %% "scala-xml"        % "1.0.4",
  "com.github.scala-incubator.io" %% "scala-io-core"    % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file"    % "0.4.3",
  "javax.mail"                    % "mail"              % "1.4",
  "com.amazonaws"                 % "aws-java-sdk-s3"   % "1.11.41",
  "com.amazonaws"                 % "aws-java-sdk-core" % "1.11.41",
  // For image resizing, I'm trying out replacing a Java thumbnailer...
  //"net.coobird"                   % "thumbnailator"     % "0.4.8",
  // ...with a wrapper around image magick:
  "org.im4java"                   % "im4java"           % "1.4.0"
)

// http://www.scala-sbt.org/0.13/docs/Forking.html

fork := true

mainClass := Some("Main")

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-Ywarn-value-discard", "-unchecked", "-deprecation", "-feature")
