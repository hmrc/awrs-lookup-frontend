/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

trait AppConfig {
  val analyticsToken: Option[String]
  val externalReportProblemUrl: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
}

class FrontendAppConfig @Inject()(servicesConfig: ServicesConfig, val mode: RunMode, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getConfString(key, throw new Exception(s"Missing configuration key: $key"))

  private val contactFrontendService = servicesConfig.baseUrl("contact-frontend")
  private val contactHost = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier = "AWRS-LOOKUP"

  val urBannerLink: String = loadConfig("urBanner.external-urls.ur-page")

  val showUrBanner: Boolean = loadConfig("urBanner.toggled").toBoolean

  override lazy val analyticsToken: Option[String] = Some(servicesConfig.getString(s"google-analytics.token"))
  override lazy val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")
  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports?secure=true"

  override lazy val externalReportProblemUrl = s"$contactHost/contact/problem_reports"

  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

}
