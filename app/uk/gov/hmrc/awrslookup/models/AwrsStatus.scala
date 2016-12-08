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

sealed trait AwrsStatus {
  def code: String

  def name: String

  override def toString: String = f"$name($code)"
}

object AwrsStatus {

  val allStatus: Set[AwrsStatus] =
    Set(NoStatus,
      Pending,
      Withdrawal,
      Approved,
      ApprovedWithConditions,
      Rejected,
      RejectedUnderReviewOrAppeal,
      Revoked,
      RevokedUnderReviewOrAppeal,
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
    case NoStatus.code => NoStatus
    case Pending.code => Pending
    case Withdrawal.code => Withdrawal
    case Approved.code => Approved
    case ApprovedWithConditions.code => ApprovedWithConditions
    case Rejected.code => Rejected
    case RejectedUnderReviewOrAppeal.code => RejectedUnderReviewOrAppeal
    case Revoked.code => Revoked
    case RevokedUnderReviewOrAppeal.code => RevokedUnderReviewOrAppeal
    case DeRegistered.code => DeRegistered
    case _ => NotFound(code)
  }

  case object NoStatus extends AwrsStatus {
    val code = "00"
    val name = "None"
  }

  case object Pending extends AwrsStatus {
    val code = "01"
    val name = "Pending"
  }

  case object Withdrawal extends AwrsStatus {
    val code = "02"
    val name = "Withdrawal"
  }

  case object Approved extends AwrsStatus {
    val code = "04"
    val name = "Approved"
  }

  case object ApprovedWithConditions extends AwrsStatus {
    val code = "05"
    val name = "Approved with Conditions"
  }

  case object Rejected extends AwrsStatus {
    val code = "06"
    val name = "Rejected"
  }

  case object RejectedUnderReviewOrAppeal extends AwrsStatus {
    val code = "07"
    val name = "Rejected under Review/Appeal"
  }

  case object Revoked extends AwrsStatus {
    val code = "08"
    val name = "Revoked"
  }

  case object RevokedUnderReviewOrAppeal extends AwrsStatus {
    val code = "09"
    val name = "Revoked under Review/Appeal"
  }

  case object DeRegistered extends AwrsStatus {
    val code = "10"
    val name = "Deregistered"
  }

  case class NotFound(code: String) extends AwrsStatus {
    val name = "Not Found"
  }
}
