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

package utils

import play.api.data.Form
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import models.AwrsStatus.Approved
import models._
import utils.ImplicitConversions._

object TestUtils {

  def populateFakeRequestWithPost[T](fakeRequest: FakeRequest[_], form: Form[T], data: T): FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequest.withFormUrlEncodedBody(form.fill(data).data.toSeq: _*)

  val testAwrsRef = "XXAW00000123455"

  val testInfo: String => Info = (id: String) => Info(s"testBusinessName$id", s"testTradingName$id", s"testFullName$id",
    Address(s"testline1$id", s"testline2$id", s"testline3$id", s"testline4$id", s"testPostCode$id", s"testCountry$id"))

  def testBusiness(ref: String = testAwrsRef): Business = Business(ref, "1 April 2017", Approved, testInfo(" bus"))

  def testGroup(ref: String = testAwrsRef): Group = Group(ref, "1 April 2017", Approved, testInfo(" group"),
    List(testInfo(" member 1"), testInfo(" member 2"), testInfo(" member 3"), testInfo(" member 4")))

  val testBusinessSearchResult: SearchResult = SearchResult(List(testBusiness()))

  val testGroupSearchResult: SearchResult = SearchResult(List(testGroup()))

}
