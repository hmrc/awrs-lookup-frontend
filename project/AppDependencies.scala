import sbt._

object AppDependencies {
  import play.sbt.PlayImport._

  private val bootstrapVersion         = "8.4.0"
  private val playVersion              = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials-play-30"      % "9.1.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test: Seq[sbt.ModuleID] = Seq(
        "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % scope,
        "org.jsoup"   %  "jsoup"                  % "1.17.2"         % scope,
        "org.mockito" %  "mockito-all"            % "1.10.19"        % scope,
        "org.mockito" %  "mockito-core"           % "5.10.0"         % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
