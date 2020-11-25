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

package uk.gov.hmrc.awrslookup.controllers

import java.net.URI

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.util.Try

class AwrsLanguageController @Inject()(configuration: ServicesConfig,
                                       mcc: MessagesControllerComponents,
                                       override implicit val messagesApi: MessagesApi)
  extends FrontendController(mcc) with I18nSupport {

  val English: Lang = Lang("en")
  val Welsh: Lang = Lang("cy")

  def langToCall(lang: String): Call = routes.AwrsLanguageController.switchToLanguage(lang)

  def languageMap: Map[String, Lang] = Map("english" -> English,
    "cymraeg" -> Welsh)

  private val SwitchIndicatorKey = "switching-language"
  private val FlashWithSwitchIndicator = Flash(Map(SwitchIndicatorKey -> "true"))

  protected[controllers] def fallbackURL: String = configuration.getConfString("language.fallbackUrl", "/")

  private[controllers] def asRelativeUrl(url: String): Option[String] = {
    for {
      uri      <- Try(new URI(url)).toOption
      path     <- Option(uri.getPath).filterNot(_.isEmpty)
      query    <- Option(uri.getQuery).map("?" + _).orElse(Some(""))
      fragment <- Option(uri.getRawFragment).map("#" + _).orElse(Some(""))
    } yield s"$path$query$fragment"
  }

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang = languageMap.getOrElse(language, English)

    val redirectURL = request.headers.get(REFERER)
      .flatMap(asRelativeUrl)
      .getOrElse(fallbackURL)

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(FlashWithSwitchIndicator)
  }
}
