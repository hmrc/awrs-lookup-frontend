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

package uk.gov.hmrc.awrslookup.models

import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits


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

      add1.equals(add1a) shouldBe true
      add1.equals(add2) shouldBe false
      add1.equals(add3) shouldBe false
      add2.equals(add3) shouldBe false

      add1.hashCode shouldBe add1a.hashCode()
      add1.hashCode should not be add2.hashCode()
      add3.hashCode should not be add2.hashCode()
      add3.hashCode should not be add1.hashCode()

      add1.toString shouldBe add1.toStringSeq.reduce(_+", "+_)
      add2.toString shouldBe add2.toStringSeq.reduce(_+", "+_)
      add3.toString shouldBe add3.toStringSeq.reduce(_+", "+_)
    }

    "toStringSeq should only return a sequence of the populated fields" in {
      val test = Address(line1, line2, addressLine4 = line4, addressCountry = country)
      val expected = Seq(line1, line2, line4, country)
      test.toStringSeq shouldBe expected

      val test2 = Some(test)
      test2.toStringSeq shouldBe expected
    }

  }
}
