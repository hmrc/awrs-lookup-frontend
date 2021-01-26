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

package controllers

import controllers.Assets.Redirect
import controllers.AwrsLanguageController
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.Lang
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{cookies, defaultAwaitTimeout}
import utils.AwrsUnitTestTraits
import uk.gov.hmrc.http.{SessionKeys => HMRCSessionKeys}

class AwrsLanguageControllerTest extends AwrsUnitTestTraits {

  object TestLanguageController extends AwrsLanguageController(servicesConfig, mcc, messagesApi)

  ".langCall" should {
    "return a GET call with the correct url" in {
      val call: Call = Call("GET", "/check-the-awrs-register/language/en")
      TestLanguageController.langToCall("en") mustBe call
    }
  }

  ".languageMap" should {
    "return a return a map of supported languages" in {
      val English: Lang = Lang("en")
      val Welsh: Lang = Lang("cy")

      TestLanguageController.languageMap mustBe Map("english" -> English, "cymraeg" -> Welsh)
    }
  }

  ".fallBackURL" should {
    "return a return correct config string" in {

      TestLanguageController.fallbackURL mustBe "/"
    }
  }

  "asRelativeUrl" should {

    "return a relative url with a query and fragment" in {
      val url = "https://www.tax.service.gov.uk"
      val uri = "/check-the-awrs-register/language/en?q=test#test"

      TestLanguageController.asRelativeUrl(url + uri) mustBe Some(uri)
    }

    "return a relative url without a query or fragment" in {
      val url = "https://www.tax.service.gov.uk"
      val uri = "/check-the-awrs-register/language/en"

      TestLanguageController.asRelativeUrl(url + uri) mustBe Some(uri)
    }
  }

  ".switchToLanguage" should {

    implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(HMRCSessionKeys.sessionId -> "id")

    "redirect to the referrer with the correct english language and correct toggle setting" in {

      val headers: Headers = new Headers(Seq(("referer", "https://www.tax.service.gov.uk/check-the-awrs-register/language/en?q=test#test")))
      val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "fakeUri", headers, AnyContent.apply())
      implicit val messagesRequest: MessagesRequest[AnyContent] = new MessagesRequest[AnyContent](fakeRequest, messagesApi)

      val result = await(TestLanguageController.switchToLanguage("english")(messagesRequest)).toString()

      result mustBe Redirect("/check-the-awrs-register/language/en?q=test#test").withLang(Lang("en")).toString()
    }

    "use the English language" in {
      val result = TestLanguageController.switchToLanguage("english")(fakeRequest)
      cookies(result).get(messagesApi.langCookieName) mustBe
        Some(Cookie("PLAY_LANG", "en", None, "/", None, secure = false, httpOnly = false, Some(Cookie.SameSite.Lax)))
    }

    "redirect to the referrer with the correct welsh language and correct toggle setting" in {

      val headers: Headers = new Headers(Seq(("referer", "https://www.tax.service.gov.uk/check-the-awrs-register/language/cy?q=test#test")))
      val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "fakeUri", headers, AnyContent.apply())
      implicit val messagesRequest: MessagesRequest[AnyContent] = new MessagesRequest[AnyContent](fakeRequest, messagesApi)

      val result = await(TestLanguageController.switchToLanguage("random")(messagesRequest)).toString()

      result mustBe Redirect("/check-the-awrs-register/language/cy?q=test#test").withLang(Lang("cy")).toString()
    }

    "use the Welsh language" in {
      val result = TestLanguageController.switchToLanguage("cymraeg")(fakeRequest)
      cookies(result).get(messagesApi.langCookieName) mustBe
        Some(Cookie("PLAY_LANG", "cy", None, "/", None, secure = false, httpOnly = false, Some(Cookie.SameSite.Lax)))
    }

    "redirect to the referrer with the default language and correct toggle setting" in {

      val headers: Headers = new Headers(Seq(("referer", "https://www.tax.service.gov.uk/check-the-awrs-register/language/otherLang?q=test#test")))
      val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "fakeUri", headers, AnyContent.apply())
      implicit val messagesRequest: MessagesRequest[AnyContent] = new MessagesRequest[AnyContent](fakeRequest, messagesApi)

      val result = await(TestLanguageController.switchToLanguage("otherLang")(messagesRequest)).toString()

      result mustBe Redirect("/check-the-awrs-register/language/otherLang?q=test#test").withLang(Lang("en")).toString()
    }

    "use the English language when unsupported language is used" in {
      val result = TestLanguageController.switchToLanguage("nonsupported")(fakeRequest)
      cookies(result).get(messagesApi.langCookieName) mustBe
        Some(Cookie("PLAY_LANG", "en", None, "/", None, secure = false, httpOnly = false, Some(Cookie.SameSite.Lax)))
    }
  }

}
