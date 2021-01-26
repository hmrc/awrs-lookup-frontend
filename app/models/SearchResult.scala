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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.AwrsNumberFormatter
import scala.util.matching.Regex

sealed trait AwrsEntry {

  def awrsRef: String

  @inline final def awrsRefFormatted: String = AwrsNumberFormatter.format(awrsRef)

  def registrationDate: Option[String]

  def registrationEndDate: Option[String]

  def status: AwrsStatus

  def info: Info

}

case class SearchResult(results: List[AwrsEntry])


case class Business(awrsRef: String,
                    registrationDate: Option[String],
                    status: AwrsStatus,
                    info: Info,
                    registrationEndDate: Option[String] = None
                   ) extends AwrsEntry

case class Group(awrsRef: String,
                 registrationDate: Option[String],
                 status: AwrsStatus,
                 info: Info,
                 members: List[Info],
                 registrationEndDate: Option[String] = None
                ) extends AwrsEntry

object Business {
  implicit val formatter: OFormat[Business] = Json.format[Business]
}

object Group {
  implicit val formatter: OFormat[Group] = Json.format[Group]
}

object AwrsEntry {

  val awrsFormatPattern: Regex = "([A-Za-z]{4})([0-9]{3})([0-9]{4})([0-9]{4})".r

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

  implicit val reads: Reads[AwrsEntry] = (
    (JsPath \ "class").read[String] and (JsPath \ "data").read[JsValue]
    )(AwrsEntry.apply _)

  implicit val writes: OWrites[AwrsEntry] = (
    (JsPath \ "class").write[String] and (JsPath \ "data").write[JsValue]
  )(unlift(AwrsEntry.unapply))
}

object SearchResult {
  implicit val formatter: OFormat[SearchResult] = Json.format[SearchResult]
}
