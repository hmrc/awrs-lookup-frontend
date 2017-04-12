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

package uk.gov.hmrc.awrslookup.views.html.helpers

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import uk.gov.hmrc.awrslookup.models.AwrsStatus.Approved
import uk.gov.hmrc.awrslookup.models.{Address, Group, Info}
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, HtmlUtils, TestUtils}
import TestUtils._

import scala.collection.JavaConversions._

class packageTest extends AwrsUnitTestTraits with HtmlUtils {

  // this is shadowed so we would use the implicit conversion defined in the package instead
  override def convertToOption[T, U <: T](value: U): Option[T] = ???

  "theTime function" should {
    val testDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    "format the time correctly" in {
      val amTime = DateTime.parse("04/02/2011 08:27:05", testDateFormat)
      val pmTime = DateTime.parse("04/12/2011 20:27:05", testDateFormat)
      theTime(amTime) shouldBe "4 February 2011 8:27am"
      theTime(pmTime) shouldBe "4 December 2011 8:27pm"
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
      membersStr shouldBe expectedMembersStr + "."
    }

    "return the singular version of the lede if there is only 1 member" in testLeadingMessage(1, "awrs.lookup.results.group_lede_singular")

    (2 to 4).foreach(num =>
      s"return the plural version of the lede if for $num member" in testLeadingMessage(num, "awrs.lookup.results.group_lede_plural")
    )

  }

  val testInfo = TestUtils.testInfo("")
  val epsilon = 1e-2f

  "infoMatchCoEff" should {
    val test = testInfo.copy(businessName = "FRANCE")
    infoMatchCoEff(test, "france") shouldBe 1.0 +- epsilon
    infoMatchCoEff(test, "REPUBLIC OF FRANCE") shouldBe 0.56 +- epsilon

    val test2 = testInfo.copy(tradingName = "FRANCE")
    infoMatchCoEff(test2, "france") shouldBe 1.0 +- epsilon
    infoMatchCoEff(test2, "REPUBLIC OF FRANCE") shouldBe 0.56 +- epsilon
  }

  "bestMatchName" should {
    "return either the trading/business name that is the best match to the search term" in {
      val testName = "FRANCE"
      val testName2 = "REPUBLIC OF FRANCE"
      val test = testInfo.copy(businessName = testName, tradingName = testName2)
      bestMatchName(test, testName) shouldBe testName
      bestMatchName(test, testName2) shouldBe testName2
      bestMatchName(test, "REPUBLIC") shouldBe testName2
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
      info shouldBe testInfo.copy(businessName = "my bus")
    }
  }

  val testGroup = TestUtils.testGroup("")

  "groupSearchBestMatchInfo" should {
    "return the group rep's name if the search term matches on the group rep" in {
      val testValue = "FRANCE"
      val g = testGroup.copy(info = testInfo.copy(businessName = testValue))
      val actual = groupSearchBestMatchInfo(g, searchTerm = testValue)
      actual shouldBe testValue
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
      actual shouldBe s"$testValue part of the ${knownName(testGroup.info)}"
    }
  }

}
