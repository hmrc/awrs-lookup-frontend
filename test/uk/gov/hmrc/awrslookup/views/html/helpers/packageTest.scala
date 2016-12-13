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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import uk.gov.hmrc.awrslookup.models.AwrsStatus.Approved
import uk.gov.hmrc.awrslookup.models.{Group, Info}
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, HtmlUtils}

import scala.collection.JavaConversions._

class packageTest extends AwrsUnitTestTraits with HtmlUtils {

  // this is shadowed so we would use the implicit conversion defined in the package instead
  override def convertToOption[T, U <: T](value: U): Option[T] = ???

  "theTime function" should {
    val testDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    "format the time correctly" in {
      val amTime = DateTime.parse("04/02/2011 08:27:05", testDateFormat)
      val pmTime = DateTime.parse("04/12/2011 20:27:05", testDateFormat)
      theTime(amTime) shouldBe "04 February 2011 08:27 am"
      theTime(pmTime) shouldBe "04 December 2011 08:27 pm"
    }
  }

  "spans function" should {
    "Output only defined elements in <span> tags" in {
      val testData: Map[String, Option[String]] = Map[String, Option[String]]("addressLine1" -> "line1", "addressLine2" -> "line2", "addressLine3" -> None, "addressLine4" -> "line4")
      val soupDoc: Document = spans(testData)
      val spanTags = soupDoc.getElementsByTag("span")
      withClue(s"span tags found:\n$spanTags\n") {
        spanTags.size() shouldBe 3
      }
      testData.foreach {
        case (id: String, Some(x)) =>
          spanTags.text().contains(x) shouldBe true
        case _ =>
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

    // this function keeps the beginning of the string stopping at the first {
    // i.e. should end at the location of the first variable argument to the message
    def leadingString(str: String) = str.substring(0, str.indexOf("{1}"))

    def testLeadingMessage(numMembers: Int, msgKey: String) = {
      val testData = testGroup(numMembers)
      val soupDoc: Document = groupLedge(testData)
      val text = soupDoc.text()
      val singular = Messages(msgKey, knownName(testData.info)).toString.htmlTrim
      val leadStr = leadingString(singular)

      text should startWith(leadStr)

      // test the llinks exists and are correctly indexed
      val links = soupDoc.getElementsByTag("a")
      links.size() shouldBe testData.members.size
      links.zipWithIndex.foreach {
        case (link, ind) =>
          link.attr("href") shouldBe s"#result_member_${ind}_heading"
      }

      // test the text are correct:
      // if only 1 member: member 1
      // if only 2 members: member 1 and member2
      // if more than 2 members: member 1, member2, ..., memberN-1 and memberN
      val membersStr = text.replaceFirst(leadStr, "")
      val expectedMembersStr = testData.members.size match {
        case 1 => knownName(testData.members.head)
        case 2 => knownName(testData.members.head) + " and " + knownName(testData.members(1))
        case _ =>
          val names = testData.members.map(knownName)
          names.dropRight(1).mkString(", ") + " and " + names.last
      }
      membersStr shouldBe expectedMembersStr
    }

    "return the singular version of the lede if there is only 1 member" in testLeadingMessage(1, "awrs.lookup.results.group_lede_singular")

    (2 to 4).foreach(num =>
      s"return the plural version of the lede if for $num member" in testLeadingMessage(num, "awrs.lookup.results.group_lede_plural")
    )

  }

}
