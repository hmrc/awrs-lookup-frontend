/*
 * Copyright 2017 HM Revenue & Customs
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

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, HtmlUtils}
import uk.gov.hmrc.awrslookup.utils.TestUtils._
import play.api.test.Helpers._

import scala.concurrent.Future

class LookupControllerTest extends AwrsUnitTestTraits {
  val mockLookupService: LookupService = mock[LookupService]
  val lookupFailure = Json.parse( """{"reason": "Generic test reason"}""")

  object TestLookupController extends LookupController(environment = environment, configuration = configuration, messagesApi = messagesApi) {
    override val lookupService: LookupService = mockLookupService
  }

  "Lookup Controller " should {

    "use the correct Lookup service" in {
      new LookupController(environment = environment, configuration = configuration, messagesApi = messagesApi).lookupService shouldBe LookupService
    }

    "in show, lookup awrs entry when passed a valid awrs reference" in {
      when(mockLookupService.lookupAwrsRef(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val result = TestLookupController.show.apply(FakeRequest())
      status(result) shouldBe OK
    }

    "in byNameShow, lookup awrs entries by name" in {
      when(mockLookupService.lookupByName(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val result = TestLookupController.byNameShow.apply(FakeRequest())
      status(result) shouldBe OK
    }
  }

  "lookup routes" should {
    import HtmlUtils._

    def callLookupFrontEndAndReturnSummaryError(query: Option[String] = None) = {
      val qString = query match {
        case Some(q) => "?query=" + q
        case _ => ""
      }
      val oResult = route(app, FakeRequest(GET, "/awrs-lookup" + qString))
      oResult shouldBe 'defined
      val result = oResult.get
      status(result) shouldBe OK
      val doc = result.getDocument
      doc.getElementById("query_errorLink")
    }

    "do not show error if there is no query string" in {
      callLookupFrontEndAndReturnSummaryError(None) shouldBe null
    }

    "show error if the query field is empty" in {
      callLookupFrontEndAndReturnSummaryError("").text shouldBe Messages("awrs.search.query.empty.summary")
    }

    "show error if the query is invalid" in {
      callLookupFrontEndAndReturnSummaryError("invalidValue").text shouldBe Messages("awrs.generic.error.character_invalid.summary", "search field")
    }

  }

}
