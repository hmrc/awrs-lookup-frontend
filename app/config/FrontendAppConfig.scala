/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import controllers.routes
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.Language

trait AppConfig {
  val analyticsToken: Option[String]
  val externalReportProblemUrl: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
}

class FrontendAppConfig @Inject()(servicesConfig: ServicesConfig, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getConfString(key, throw new Exception(s"Missing configuration key: $key"))

  private val contactFrontendService = servicesConfig.getString("contact-frontend.host")
  private val contactHost = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier = "AWRS-LOOKUP"

  val urBannerLink: String = loadConfig("urBanner.external-urls.ur-page")

  val showUrBanner: Boolean = loadConfig("urBanner.toggled").toBoolean

  lazy val cookies: String = servicesConfig.getString("urls.footer.cookies")
  lazy val accessibilityStatement: String = servicesConfig.getString("urls.footer.accessibility_statement")
  lazy val privacy: String = servicesConfig.getString("urls.footer.privacy_policy")
  lazy val termsConditions: String = servicesConfig.getString("urls.footer.terms_and_conditions")
  lazy val govukHelp: String = servicesConfig.getString("urls.footer.help_page")

  override lazy val analyticsToken: Option[String] = Some(servicesConfig.getString(s"google-analytics.token"))
  override lazy val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")
  override lazy val externalReportProblemUrl = s"$contactHost/contact/problem_reports"

  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendService/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  def languageLinks: Seq[(Language, String)] = {
    Seq(
      (En, routes.AwrsLanguageController.switchToLanguage("english").url),
      (Cy, routes.AwrsLanguageController.switchToLanguage("cymraeg").url)
    )
  }

}
