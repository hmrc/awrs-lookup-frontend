/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.services

import uk.gov.hmrc.awrslookup.connectors.LookupConnector
import uk.gov.hmrc.awrslookup.connectors.mocks.MockLookupConnector
import uk.gov.hmrc.awrslookup.models.SearchResult
import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits

import scala.concurrent.Future

class LookupServiceTest extends AwrsUnitTestTraits
  with MockLookupConnector {

  object LookupServiceTest extends LookupService(mockLookupConnector)
  "LookupService" should {
    
    "call lookup connector search by urn in lookup if the query is a valid AWRS number" in {
      val testAwrs = "XXAW00000123456"
      val notTestAwrs = ""

      assert(testAwrs != notTestAwrs)

      val dataToReturn: Future[Option[SearchResult]] = SearchResult(Nil)
      val noDataToReturn: Future[Option[SearchResult]] = None

      // set it up so that if the parameter used in lookup connector doesn't match 'testAwrs'
      // then 'noDataToReturn' is returned
      // and 'dataToReturn' is returned otherwise
      mockLookupConnectorWithOnly(queryByName = (AnyMatcher, noDataToReturn))
      mockLookupConnectorWithOnly(queryByUrn = (EqMatcher(testAwrs), dataToReturn))

      LookupServiceTest.lookup(testAwrs) shouldBe dataToReturn
      LookupServiceTest.lookup(notTestAwrs) shouldBe noDataToReturn
    }

    "call lookup connector search by name in lookup if the query is not a valid AWRS number" in {
      val testAwrs = "testvalue"
      val notTestAwrs = ""

      assert(testAwrs != notTestAwrs)

      val dataToReturn: Future[Option[SearchResult]] = SearchResult(Nil)
      val noDataToReturn: Future[Option[SearchResult]] = None

      // set it up so that if the parameter used in lookup connector doesn't match 'testAwrs'
      // then 'noDataToReturn' is returned
      // and 'dataToReturn' is returned otherwise
      mockLookupConnectorWithOnly(queryByName = (AnyMatcher, noDataToReturn)) // must be placed before the eq matcher
      mockLookupConnectorWithOnly(queryByName = (EqMatcher(testAwrs), dataToReturn))

      LookupServiceTest.lookup(testAwrs) shouldBe dataToReturn
      LookupServiceTest.lookup(notTestAwrs) shouldBe noDataToReturn
    }
  }


}
