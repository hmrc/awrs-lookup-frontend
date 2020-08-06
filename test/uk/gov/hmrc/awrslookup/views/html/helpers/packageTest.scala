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

package uk.gov.hmrc.awrslookup.views.html.helpers

import java.time.format.DateTimeFormatter

import org.jsoup.nodes.Document
import uk.gov.hmrc.awrslookup.models.AwrsStatus.Approved
import uk.gov.hmrc.awrslookup.models.{Group, Info}
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, HtmlUtils, TestUtils}

class packageTest extends AwrsUnitTestTraits with HtmlUtils {

  // this is shadowed so we would use the implicit conversion defined in the package instead
  override def convertToOption[T, U <: T](value: U): Option[T] = ???

  "spans function" should {
    "Output only defined elements in <span> tags" in {
      val testData: Map[String, Option[String]] = Map[String, Option[String]]("addressLine1" -> "line1", "addressLine2" -> "line2", "addressLine3" -> None, "addressLine4" -> "line4")
      val soupDoc: Document = spans(testData)
      val spanTags = soupDoc.getElementsByTag("span")
      withClue(s"span tags found:\n$spanTags\n") {
        spanTags.size() mustBe 3
      }
      testData.foreach {
        case (id: String, Some(x)) =>
          spanTags.text().contains(x) mustBe true
        case _ =>
      }
    }
  }

  "knownName function" should {
    val testName = "testName"
    "Return trading name if known" in {
      val info = Info(tradingName = testName, businessName = "not test name")
      knownName(info) mustBe testName
    }

    "Return business name if trading name is not known" in {
      val info = Info(businessName = testName, tradingName = None)
      knownName(info) mustBe testName
    }
  }

  val testInfo = TestUtils.testInfo("")
  val epsilon = 1e-2f

  "infoMatchCoEff" should {
    "match with specific confidence" in {
      val test = testInfo.copy(businessName = "FRANCE")
      infoMatchCoEff(test, "france") mustBe 1.0 +- epsilon
      infoMatchCoEff(test, "REPUBLIC OF FRANCE") mustBe 0.56 +- epsilon

      val test2 = testInfo.copy(tradingName = "FRANCE")
      infoMatchCoEff(test2, "france") mustBe 1.0 +- epsilon
      infoMatchCoEff(test2, "REPUBLIC OF FRANCE") mustBe 0.56 +- epsilon
    }
  }

  "bestMatchName" should {
    "return either the trading/business name that is the best match to the search term" in {
      val testName = "FRANCE"
      val testName2 = "REPUBLIC OF FRANCE"
      val test = testInfo.copy(businessName = testName, tradingName = testName2)
      bestMatchName(test, testName) mustBe testName
      bestMatchName(test, testName2) mustBe testName2
      bestMatchName(test, "REPUBLIC") mustBe testName2
    }
  }

  "memberWithTheClosestMatch" should {
    "find the best matching member" in {
      val g: Group = Group(
        awrsRef = "testValue",
        registrationDate = "01/01/1970",
        status = Approved,
        registrationEndDate = "01/01/2017",
        info = testInfo,
        members = List(
          testInfo.copy(businessName = "testBusinessName2"),
          testInfo.copy(businessName = "testBusinessName3"),
          testInfo.copy(businessName = "my bus"),
          testInfo.copy(businessName = "testBusinessName4")
        )
      )

      val info = memberWithTheClosestMatch(g.members, "my bus")
      info mustBe testInfo.copy(businessName = "my bus")
    }
  }

  val testGroup = TestUtils.testGroup("")

  "groupSearchBestMatchInfo" should {
    "return the group rep's name if the search term matches on the group rep" in {
      val testValue = "FRANCE"
      val g = testGroup.copy(info = testInfo.copy(businessName = testValue))
      val actual = groupSearchBestMatchInfo(g, searchTerm = testValue)
      actual mustBe testValue
    }

    "return the member's name if the search term matches a member" in {
      val testValue = "FRANCE"
      val g = testGroup.copy(members =
        List(
          testInfo.copy(tradingName = "not me"),
          testInfo.copy(tradingName = testValue),
          testInfo.copy(tradingName = "not me 2"),
          testInfo.copy(tradingName = "not me 3")
        )
      )
      val actual = groupSearchBestMatchInfo(g, searchTerm = testValue)
      actual mustBe s"$testValue part of the ${knownName(testGroup.info)}"
    }
  }

}
