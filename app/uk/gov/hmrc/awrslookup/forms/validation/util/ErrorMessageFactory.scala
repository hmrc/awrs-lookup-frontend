/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.forms.validation.util

import play.api.data.validation.Invalid

/**
  * This API is used to configure and create instances of play.api.data.validation.Invalid.
  * The Invalid instances are used to specify the error messages from the play framework's form validation libraries
  * (supported for play 2.4), and are used in constraints/mappings/formatter to specify the error when a constraint
  * fails.
  *
  * This API configures how the Invalid objects will be populated so that the ErrorMessageInterpreter API can
  * extract the specified information for the html views
  *
  */
trait ErrorMessageFactory {
  /**
    * This method is used to create a new Invalid instance with the specified summary error message
    * The summary error config do not need to include a message key, and if it does not then the default key will be
    * used. The default summary key will be derived from calling the defaultSummaryId("fieldId") method from
    * the ErrorMessageInterpreter API
    *
    * Note: any MessageArguments provided will only be applied to the config that it is a parameter of, i.e. either the
    * summary or the field. If the same argument is required for both the summary and field error message then it must
    * be provided for both.
    *
    * Example usages:
    * createErrorMessage(
    * TargetFieldIds("anchorId"),
    * FieldErrorConfig("fieldKey",MessageArguments("fieldArg1")),
    * SummaryErrorConfig(MessageArguments("summaryArg1","summaryArg2"))
    * )
    *
    * createErrorMessage(
    * TargetFieldIds("anchorId","otherId1","otherId2"),
    * FieldErrorConfig("fieldKey"),
    * SummaryErrorConfig("summaryKey", MessageArguments("summaryArg1"))
    * )
    *
    * @param summaryMsg the summary error specification, the summary key is still optional and the default value
    *                   will be used if it is not specified
    * @param fieldMsg   the field error specification
    * @param ids        the target ids of the fields in which the errors are intended for
    * @return the Invalid instance
    */
  def createErrorMessage(ids: TargetFieldIds, fieldMsg: FieldErrorConfig, summaryMsg: SummaryErrorConfig): Invalid

  /**
    * This method is used to create a new Invalid instance using the default summary error message
    * The default summary key will be derived from calling the defaultSummaryId("fieldId") method from
    * the ErrorMessageInterpreter API.
    * The default summary argument is "no arguments".
    *
    * Example usages:
    * createErrorMessage(
    * TargetFieldIds("anchorId"),
    * FieldErrorConfig("fieldKey")
    * )
    *
    * createErrorMessage(
    * TargetFieldIds("anchorId","otherId1","otherId2"),
    * FieldErrorConfig("fieldKey",MessageArguments("fieldArg1","fieldArg2"))
    * )
    *
    * @param fieldMsg the field error specification
    * @param ids      the target ids of the fields in which the errors are intended for
    * @return the Invalid instance
    */
  def createErrorMessage(ids: TargetFieldIds, fieldMsg: FieldErrorConfig): Invalid
}


// used to create API supported Invalid instances that can then be used in form constraints
object ErrorMessageFactory extends ErrorMessageFactory {

  import ErrorDelimiterConstants._

  private def argsToString(args: Any): String =
    args match {
      case EmbeddedMessage(x, y, _) => f"${embeddedStart}%s${mkFieldErr(x, y)}%s${embeddedEnd}%s"
      case _ => args.toString
    }

  private def mkSummaryErr(id: Option[String], msgArgs: MessageArguments): String = id match {
    case None => msgArgs.args.toList map (arg => argsToString(arg)) mkString (paramDelimiter)
    case _ => List[String](id.get) ::: List(msgArgs.args.toList map (param => argsToString(param)) mkString (paramDelimiter)) mkString (summaryIdMarker)
  }

  private def mkFieldErr(msgkey: String, msgArgs: MessageArguments): String = List[String](msgkey) ::: msgArgs.args.toList map (param => argsToString(param)) mkString (paramDelimiter)

  def createErrorMessage(ids: TargetFieldIds, fieldMsg: FieldErrorConfig, summaryMsg: SummaryErrorConfig): Invalid =
    createErrorMessage(List[String](mkSummaryErr(summaryMsg.msgKey, summaryMsg.msgArgs)) ::: List(mkFieldErr(fieldMsg.msgKey, fieldMsg.msgArgs)) mkString (fieldDelimiter), ids)

  def createErrorMessage(ids: TargetFieldIds, fieldMsg: FieldErrorConfig): Invalid =
    createErrorMessage(mkFieldErr(fieldMsg.msgKey, fieldMsg.msgArgs), ids)

  private def createErrorMessage(errorMsg: String, ids: TargetFieldIds): Invalid =
    Invalid(errorMsg, ids)

}
