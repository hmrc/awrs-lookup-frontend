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

sealed trait AwrsEntry {

  def awrsRef: String

  def registrationDate: String

  // in case we don't get it
  def deRegistrationDate: Option[String]

  def status: AwrsStatus

  def info: Info

}

case class SearchResult(results: List[AwrsEntry])


case class Business(awrsRef: String,
                    registrationDate: String,
                    status: AwrsStatus,
                    info: Info,
                    deRegistrationDate: Option[String] = None
                   ) extends AwrsEntry

case class Group(awrsRef: String,
                 registrationDate: String,
                 status: AwrsStatus,
                 info: Info,
                 members: List[Info],
                 deRegistrationDate: Option[String] = None
                ) extends AwrsEntry


object Business {
  implicit val formatter = Json.format[Business]
}

object Group {
  implicit val formatter = Json.format[Group]
}

object AwrsEntry {
  def unapply(foo: AwrsEntry): Option[(String, JsValue)] = {
    val (prod: Product, sub) = foo match {
      case b: Business => (b, Json.toJson(b)(Business.formatter))
      case b: Group => (b, Json.toJson(b)(Group.formatter))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): AwrsEntry = {
    (`class` match {
      case "Business" => Json.fromJson[Business](data)(Business.formatter)
      case "Group" => Json.fromJson[Group](data)(Group.formatter)
    }).get
  }

  implicit val formatter = Json.format[AwrsEntry]
}

object SearchResult {
  implicit val formatter = Json.format[SearchResult]
}
