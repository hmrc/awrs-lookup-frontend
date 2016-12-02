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

import play.api.libs.json._


case class Info(businessName: Option[String] = None,
                tradingName: Option[String] = None,
                fullName: Option[String] = None,
                address: Option[Address] = None
               )

case class Address(
                    addressLine1: String,
                    addressLine2: String,
                    addressLine3: Option[String] = None,
                    addressLine4: Option[String] = None,
                    postcode: Option[String] = None,
                    addressCountry: Option[String] = None
                  ) {

  override def toString = {
    val line3display = addressLine3.map(line3 => s"$line3, ").fold("")(x => x)
    val line4display = addressLine4.map(line4 => s"$line4, ").fold("")(x => x)
    val postcodeDisplay = postcode.map(postcode1 => s"$postcode1, ").fold("")(x => x)
    val countryDisplay = addressCountry.map(country => s"$country, ").fold("")(x => x)
    s"$addressLine1, $addressLine2, $line3display, $line4display, $postcodeDisplay, $countryDisplay"
  }

  override def equals(obj: Any): Boolean = obj match {
    case that: Address =>
      that.addressLine1.equals(addressLine1) &&
        that.addressLine2.equals(addressLine2) &&
        that.addressLine3.equals(addressLine3) &&
        that.addressLine4.equals(addressLine4) &&
        that.postcode.equals(postcode) &&
        that.addressCountry.equals(addressCountry)
    case _ => false
  }

  override def hashCode(): Int =
    (addressLine1, addressLine2, addressLine3, addressLine4, postcode, addressCountry).hashCode()
}

object Address {
  implicit val formatter = Json.format[Address]
}

object Info {
  implicit val formatter = Json.format[Info]
}
