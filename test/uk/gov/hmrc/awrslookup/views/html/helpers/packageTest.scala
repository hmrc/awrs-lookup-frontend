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

package uk.gov.hmrc.awrslookup.views.html.helpers

import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import uk.gov.hmrc.awrslookup.models.AwrsStatus.Approved
import uk.gov.hmrc.awrslookup.models.{AwrsStatus, Group, Info}

class packageTest extends AwrsUnitTestTraits {

  // this is shadowed so we would use the implicit conversion defined in the package instead
  override def convertToOption[T, U <: T](value: U): Option[T] = ???

  implicit def soupUtil(html: Html): Document = Jsoup.parse(html.toString())

  "theTime function" should {
    val testDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    "format the time correctly" in {
      val amTime = DateTime.parse("04/02/2011 08:27:05", testDateFormat)
      val pmTime = DateTime.parse("04/12/2011 20:27:05", testDateFormat)
      theTime(amTime) shouldBe "04 February 2011 08:27 am"
      theTime(pmTime) shouldBe "04 December 2011 08:27 pm"
    }
  }

  "paragraphs function" should {
    "Output only defined elements in <p> tags" in {
      val testData: Seq[Option[String]] = Seq[Option[String]]("line1", "line2", None, "line4")
      val soupDoc: Document = paragraphs(testData: _*)
      val pTags = soupDoc.getElementsByTag("p")
      pTags.size() shouldBe 3
      testData.flatten.foreach {
        x => pTags.text().contains(x) shouldBe true
      }
    }
  }

  "knownName function" should {
    val testName = "testName"
    "Return trading name if known" in {
      val info = Info(tradingName = testName, businessName = "not test name")
      knownName(info) shouldBe testName
    }

    "Return business name if trading name is not known" in {
      val info = Info(businessName = testName, tradingName = None)
      knownName(info) shouldBe testName
    }
  }

  "groupLedge function" should {

    def testInfo(member: Int) = Info(
      tradingName = s"test-$member"
    )
    def testGroup(members: Int) = Group(
      awrsRef = "",
      registrationDate = "",
      status = Approved,
      info = Info(),
      members = List.fill(members)(testInfo(members))
    )

    "display the singular version if there is only 1 memeber" in {
      val testData = testGroup(1)
      val soupDoc: Document = groupLedge(testData)
    }
  }
}
