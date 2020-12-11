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

package models

import utils.AwrsUnitTestTraits


class AddressTest extends AwrsUnitTestTraits {


  val line1 = "line1"
  val line2 = "line2"
  val line3 = "line3"
  val line4 = "line4"
  val post = "post"
  val country = "country"

  "Address" should {
    "override hashcode, equals and toString correctly" in {
      val add1 = Address(line1, line2, line3, line4, post, country)
      val add1a = Address(line1, line2, line3, line4, post, country)
      val add2 = Address(line1, line2, addressLine4 = line4, addressCountry = country)
      val add3 = Address(line1, line2)

      add1.equals(add1a) mustBe true
      add1.equals(add2) mustBe false
      add1.equals(add3) mustBe false
      add2.equals(add3) mustBe false
      add1.equals(null) mustBe false

      add1.hashCode mustBe add1a.hashCode()
      add1.hashCode must not be add2.hashCode()
      add3.hashCode must not be add2.hashCode()
      add3.hashCode must not be add1.hashCode()

      add1.toString mustBe add1.toStringSeq.reduce(_ + ", " + _)
      add2.toString mustBe add2.toStringSeq.reduce(_ + ", " + _)
      add3.toString mustBe add3.toStringSeq.reduce(_ + ", " + _)
    }

    "toStringSeq should only return a sequence of the populated fields" in {
      val test = Address(line1, line2, addressLine4 = line4, addressCountry = country)
      val expected = Seq(line1, line2, line4, country)
      test.toStringSeq mustBe expected

      val test2 = Some(test)
      test2.toStringSeq mustBe expected

      val none: Option[Address] = None
      none.toStringSeq mustBe Seq()
    }

  }
}
