import sbt._

object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playPartialsVersion      = "8.3.0-play-28"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val pegdownVersion           = "1.6.0"
  private val mockitoVersion           = "1.10.19"
  private val playLanguageVersion      = "5.3.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.25.0",
    "uk.gov.hmrc" %% "play-partials"              % playPartialsVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "3.31.0-play-28",
    "uk.gov.hmrc" %% "play-language"              % playLanguageVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.25.0"                 % scope,
        "org.scalatestplus.play" %% "scalatestplus-play"     % scalaTestPlusPlayVersion % scope,
        "org.pegdown"            %  "pegdown"                % pegdownVersion           % scope,
        "org.jsoup"              %  "jsoup"                  % "1.15.3"                 % scope,
        "com.typesafe.play"      %% "play-test"              % PlayVersion.current      % scope,
        "org.mockito"            %  "mockito-all"            % mockitoVersion           % scope,
        "org.mockito"            %  "mockito-core"           % "4.8.0"                  % scope,
        "org.scalatestplus"      %% "mockito-3-12"           % "3.2.10.0"               % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
