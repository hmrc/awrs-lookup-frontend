/*
 * Copyright 2022 HM Revenue & Customs
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AwrsDateFormatterTest extends AwrsUnitTestTraits with HtmlUtils {

  val datePattern = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  "Calling showDateTranslation" should {
    
    "returns a translateable date string from a string that matches the format (d MMMM yyyy)" in {
      val dateString = "1 April 2017"
      AwrsDateFormatter.showDateTranslation(dateString) mustBe "1 April 2017"
    }

    "returns empty string when given date does not match the required date format (d MMMM yyyy)" in {
      val dateString1 = "sdfsdfssdfs"
      val dateString2 = "2122-33-22"
      val dateString3 = ""
      AwrsDateFormatter.showDateTranslation(dateString1) mustBe ""
      AwrsDateFormatter.showDateTranslation(dateString2) mustBe ""
      AwrsDateFormatter.showDateTranslation(dateString3) mustBe ""
    }
  }

  "Calling showDateTimeNowTranslation" should {
    "returns a translateable date and time string" in {
      val amTime = LocalDateTime.parse("2011-02-04 08:27:05", dateTimePattern)
      val pmTime = LocalDateTime.parse("2011-12-04 20:27:05", dateTimePattern)
      AwrsDateFormatter.showDateTimeNowTranslation(amTime) mustBe "4 February 2011 8:27am"
      AwrsDateFormatter.showDateTimeNowTranslation(pmTime) mustBe "4 December 2011 8:27pm"
    }
  }

  "Calling setDateTimeFormat" when {
    val date = LocalDateTime.parse("2011-05-04 12:55:22", dateTimePattern)
    val monthValue = date.getMonthValue

    "showTime is true" should {
      "returns a LocalDateTime format with a time correctly" in {
        AwrsDateFormatter.setDateTimeFormat(monthValue, true) mustBe "d 'May' uuuu h:mma"
      }
    }

    "showTime is false" should {
      "returns a LocalDateTime format without a time correctly" in {
        AwrsDateFormatter.setDateTimeFormat(monthValue, false) mustBe "d 'May' uuuu"
      }
    }
  }
}
