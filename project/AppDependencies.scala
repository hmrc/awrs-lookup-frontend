import sbt._

object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playPartialsVersion      = "8.4.0-play-28"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val mockitoVersion           = "1.10.19"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.22.0",
    "uk.gov.hmrc" %% "play-partials"              % playPartialsVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.19.0-play-28"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "7.22.0"                 % scope,
        "org.scalatestplus.play" %% "scalatestplus-play"     % scalaTestPlusPlayVersion % scope,
        "org.jsoup"              %  "jsoup"                  % "1.16.2"                 % scope,
        "com.typesafe.play"      %% "play-test"              % PlayVersion.current      % scope,
        "org.mockito"            %  "mockito-all"            % mockitoVersion           % scope,
        "org.mockito"            %  "mockito-core"           % "5.7.0"                  % scope,
        "org.scalatestplus"      %% "mockito-4-11"           % "3.2.17.0"               % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
