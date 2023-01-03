/*
 * Copyright 2023 HM Revenue & Customs
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

sealed trait AwrsStatus {
  def code: String

  def name: String

  override def toString: String = f"$name($code)"
}

object AwrsStatus {

  val allStatus: Set[AwrsStatus] =
    Set(Approved,
      Revoked,
      DeRegistered)

  implicit val reader: Reads[AwrsStatus] = new Reads[AwrsStatus] {
    def reads(json: JsValue): JsResult[AwrsStatus] =
      JsSuccess(json match {
        case JsString(code) => apply(code)
        case _ => apply("-01")
      })
  }

  implicit val writer: Writes[AwrsStatus] = new Writes[AwrsStatus] {
    def writes(v: AwrsStatus): JsValue = JsString(v.code)
  }

  def apply(code: String): AwrsStatus = code match {
    case Approved.code => Approved
    case Revoked.code => Revoked
    case DeRegistered.code => DeRegistered
    case _ => NotFound(code)
  }

  case object Approved extends AwrsStatus {
    val code = "04"
    val name = "Approved"
  }

  case object Revoked extends AwrsStatus {
    val code = "08"
    val name = "No longer registered"
  }

  case object DeRegistered extends AwrsStatus {
    val code = "10"
    val name = "No longer registered"
  }

  case class NotFound(code: String) extends AwrsStatus {
    val name = "Not Found"
  }
}
