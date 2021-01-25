/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json._

case class Info(businessName: Option[String] = None,
                tradingName: Option[String] = None,
                fullName: Option[String] = None,
                address: Option[Address] = None
               )

case class Address(
                    addressLine1: Option[String] = None,
                    addressLine2: Option[String] = None,
                    addressLine3: Option[String] = None,
                    addressLine4: Option[String] = None,
                    postcode: Option[String] = None,
                    addressCountry: Option[String] = None
                  ) {

  override def toString = toStringSeq.mkString(", ")


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

  def toStringSeq: Seq[String] = {
    Seq[Option[String]](addressLine1, addressLine2, addressLine3, addressLine4, postcode, addressCountry).flatten
  }

}

object Address {
  implicit val formatter = Json.format[Address]

  implicit class AddressUtil(address: Option[Address]) {
    def toStringSeq: Seq[String] = address.fold(Seq[String]())(x => x.toStringSeq)
  }

}

object Info {
  implicit val formatter = Json.format[Info]
}
