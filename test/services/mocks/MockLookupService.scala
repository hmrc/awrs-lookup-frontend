/*
 * Copyright 2023 HM Revenue & Customs
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

package services.mocks

import org.mockito.Matchers
import org.mockito.Mockito._
import models.SearchResult
import services.LookupService
import utils.AwrsUnitTestTraits

import scala.concurrent.Future

trait MockLookupService extends AwrsUnitTestTraits {

  val mockLookupService = mock[LookupService]

  def mockLookupServiceWithOnly(lookupAwrsRef: MockConfiguration[Future[Option[SearchResult]]] = DoNotConfigure): Unit = {
    lookupAwrsRef ifConfiguredThen (dataToReturn => when(mockLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(dataToReturn))
  }
}
