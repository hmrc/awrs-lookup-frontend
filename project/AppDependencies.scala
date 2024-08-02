import sbt.*

object AppDependencies {
  import play.sbt.PlayImport.*

  private val bootstrapVersion         = "9.1.0"
  private val playVersion              = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials-play-30"      % "10.0.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup"   %  "jsoup"                  % "1.18.1"         % Test,
    "org.mockito" %  "mockito-all"            % "1.10.19"        % Test,
    "org.mockito" %  "mockito-core"           % "5.12.0"         % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
