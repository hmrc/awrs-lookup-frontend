/*
 * Copyright 2016 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits
import uk.gov.hmrc.awrslookup.utils.TestUtils._

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

    "lookup awrs entry when passed a valid awrs reference" in {
      when(mockLookupService.lookupAwrsRef(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val result = TestLookupController.show.apply(FakeRequest())
      status(result) shouldBe OK
    }
  }
}