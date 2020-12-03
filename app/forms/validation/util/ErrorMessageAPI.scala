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

/** ********************************************************************************************************************
  * This package is designed to centralise both the process for specifying the content of the validation error messages
  * and the process for determining how the html templates should then be populated.
  *
  * This source file specifies the data types used throughout the package, which is in turn divided into 3 APIs
  *
  * 1) ErrorMessageFactory
  * This API is used to create new play.api.data.validation.Invalid objects, these instances are used in the play
  * framework's validation library and are then returned in case of form errors.
  *
  * 2) ErrorMessageInterpreter
  * This API is used to convert play.api.data.validation.Invalid objects created by the ErrorMessageFactory into
  * data structures that can then be easily used to populate the error fields in the HTML templates,
  * and/or converted into their relevant error messages
  *
  * 3) ErrorMessageLookup
  * This API is used to convert the output of ErrorMessageInterpreter into the messages specified by the values
  * specified in the conf/messages file
  *
  *
  * Process workflow:
  *
  * i) The ErrorMessageFactory API is used to define the Invalid instances that can then be used by form
  * mappings/constraints/formatter
  *
  * ii) On form validation failure, the ErrorMessageInterpreter API is used to extract the summary and field errors
  * from the "form with errors".
  *
  * iii) Use the ErrorMessageLookup API to evaluate the outputs to ErrorMessageInterpreter API into the underlying
  * messages
  *
  * ********************************************************************************************************************/
package forms.validation.util

import play.api.i18n.{I18nSupport, Messages, MessagesApi}

/**
  * MessageArguments is a case class used in the MessageConfig objects to specify the message arguments in the
  * conf/messages file
  *
  * Note: an argument can be set to "", this will essentially nullify the argument
  * e.g. when used in the message string "hello {0} world"
  * this will return "hello   world" and when this is used in html it will strip out the additional whitespaces
  * and turn it into "hello world"
  *
  * If the number of arguments provided by this field exceeds the message in conf/messages then the additional
  * parameters will be ignored
  */
case class MessageArguments(args: Any*)

/**
  * MessageConfig is a trait which specifies the configuration of a message to be used by the ErrorMessageFactory API.
  * These are defined by a message key and a an optional sequence of arguments expected in the conf/messages file.
  *
  * This type is different from its subtype MessageLookup trait in that not all types using this trait will have a
  * message key specified and therefore cannot be used directly to evaluate the message
  * e.g. SummaryErrorConfig
  */
trait MessageConfig[T] {
  def msgKey: T

  def msgArgs: MessageArguments
}

/**
  * FieldErrorConfig is a required parameter in the ErrorMessageFactory API used to specify the field error message.
  *
  * It is also used to create default configuration for summary error messages if they are not specified otherwise
  *
  * Example usages:
  *
  * FieldErrorConfig("fieldKey")
  * FieldErrorConfig("fieldKey", MessageArguments("summaryArg1"))
  * FieldErrorConfig("fieldKey", MessageArguments("summaryArg1","summaryArg2"))
  *
  * @param msgKey  the key in the conf/messages file
  * @param msgArgs any arguments expected by the message in the conf/messages file
  */
case class FieldErrorConfig(msgKey: String, msgArgs: MessageArguments = MessageArguments()) extends MessageConfig[String]

/**
  * SummaryErrorConfig is an optional parameter in the ErrorMessageFactory API used to specify the summary error message.
  *
  * If this parameter is  not specified then default summary error configurations derived from the field error
  * configurations will be used
  *
  * Example usages:
  *
  * SummaryErrorConfig(Some("summaryKey"))
  * SummaryErrorConfig("summaryKey")
  * SummaryErrorConfig("summaryKey", MessageArguments("summaryArg1"))
  * SummaryErrorConfig(MessageArguments("summaryArg1","summaryArg2"))
  *
  * @param msgKey  optional parameter, the key in the conf/messages file. If left unspecified then the default key will
  *                be used based on the field error configuration
  * @param msgArgs any arguments expected by the message in the conf/messages file
  */
case class SummaryErrorConfig(msgKey: Option[String], msgArgs: MessageArguments = MessageArguments()) extends MessageConfig[Option[String]] {
  def this(msgArgs: MessageArguments) = this(None, msgArgs)

  def this(msgKey: String, msgArgs: MessageArguments) = this(Some(msgKey), msgArgs)
}

object SummaryErrorConfig {
  def apply(msgArgs: MessageArguments): SummaryErrorConfig = new SummaryErrorConfig(msgArgs)

  def apply(msgKey: String, msgArgs: MessageArguments): SummaryErrorConfig = new SummaryErrorConfig(msgKey, msgArgs)
}

/**
  * TargetFieldIds is a required parameter in the ErrorMessageFactory API used to specify the field(s) in which the
  * error message is intended for.
  *
  * The error message may apply to only a single or a collection of fields, however only 1 summary error will be set
  * regardless.
  *
  * The anchor is a required parameter as this specifies which of the fields the summary error will be hyper linked to
  *
  * Note: Although not enforced, the arguments in the otherids should be a set, and the anchor should NOT be a member
  * of otherIds
  *
  * Example usages:
  *
  * TargetFieldIds("anchorId")
  * TargetFieldIds("anchorId","secondFieldId")
  * TargetFieldIds("anchorId","secondFieldId","thirdFieldId")
  *
  * @param anchor   is where the link from the summary error would direct the user when it is clicked
  * @param otherIds other fields where the field error should also be displayed for
  */
case class TargetFieldIds(anchor: String, otherIds: String*)


/**
  * MessageLookup is a trait used to specify the types designed to be used by the ErrorMessageLookup API.
  * All types inheriting this trait must contain a message key.
  */
trait MessageLookup extends MessageConfig[String] with I18nSupport {
  val messagesApi: MessagesApi
  val messages: Messages

  override def toString: String = ErrorMessageLookup.messageLookup(this)(messages, messagesApi)
}

/**
  * EmbeddedMessage is designed as a paramter using in FieldErrorConfig, SummaryErrorConfig or another EmbeddedMessage
  * to reference another message in the conf/messages file
  *
  * Example usages:
  *
  * FieldErrorConfig("fieldKey",
  * MessageArguments(EmbeddedMessage("embeddedKey",MessageArguments("embeddedArg1")))
  * )
  *
  * FieldErrorConfig(
  * "fieldKey",
  * MessageArguments(
  * EmbeddedMessage("embeddedKey",MessageArguments("embeddedArg1"))
  * EmbeddedMessage("embeddedKey2",MessageArguments("embeddedArg1"))
  * )
  * )
  *
  * val innerEmbedded = EmbeddedMessage("embeddedKey2",MessageArguments("embeddedArg1")
  * val embedded = EmbeddedMessage("embeddedKey",MessageArguments(innerEmbedded))
  *
  * @param msgKey  the key in the conf/messages file
  * @param msgArgs any arguments expected by the message in the conf/messages file
  */
case class EmbeddedMessage (msgKey: String, msgArgs: MessageArguments = MessageArguments())(implicit val messages: Messages, val messagesApi: MessagesApi) extends MessageLookup

object EmbeddedMessage {

  def apply(msgKey: String)(implicit messages: Messages, messagesApi: MessagesApi): EmbeddedMessage =
    new EmbeddedMessage(msgKey = msgKey)
}

/**
  * SummaryError specifies the return type from the ErrorMessageInterpreter API
  * It contain the relevant information required by the html templates in order to display the summary error messages
  *
  * @param msgKey  the key in the conf/messages file
  * @param msgArgs any arguments expected by the message in the conf/messages file
  * @param anchor  where the summary error message will hyper link to
  */
case class SummaryError (msgKey: String, msgArgs: MessageArguments = MessageArguments(), anchor: String)(implicit val messages: Messages, val messagesApi: MessagesApi) extends MessageLookup {
  def this(msgKey: String, anchor: String)(implicit messages: Messages, messagesApi: MessagesApi) = this(msgKey, MessageArguments(), anchor)

  override def hashCode(): Int = {
    var code = this.productPrefix.hashCode()
    val arr = this.productArity
    var i = 0
    while (i < arr) {
      val elem = this.productElement(i)
      code = code * 41 + (if (elem == null) 0 else elem.hashCode())
      i += 1
    }
    code
  }

  override def equals(that: Any): Boolean = {
    that match {
      case errThat: SummaryError =>
        this.toString().equals(errThat.toString()) && this.anchor.equals(errThat.anchor)
      case _ => false
    }
  }
}

/**
  * FieldError specifies the return type from the ErrorMessageInterpreter API
  * It contain the relevant information required by the html templates in order to display the field error messages
  *
  * @param msgKey  the key in the conf/messages file
  * @param msgArgs any arguments expected by the message in the conf/messages file
  */
case class FieldError (msgKey: String, msgArgs: MessageArguments = MessageArguments())(implicit val messages: Messages, val messagesApi: MessagesApi) extends MessageLookup {
  override def hashCode(): Int = {
    var code = this.productPrefix.hashCode()
    val arr = this.productArity
    var i = 0
    while (i < arr) {
      val elem = this.productElement(i)
      code = code * 41 + (if (elem == null) 0 else elem.hashCode())
      i += 1
    }
    code
  }

  override def equals(that: Any): Boolean = that match {
    case errThat: FieldError =>
      this.toString().equals(errThat.toString())
    case _ => false
  }
}

// These constaints are used by the factory and extractor to construct and extract the error messages from their configs.
// These constans only used for the string manipulation version of the implementation.
object ErrorDelimiterConstants {
  val summaryIdMarker = "#"
  val paramDelimiter = ";"
  val fieldDelimiter = ":"
  val embeddedStart = "{({"
  val embeddedEnd = "})}"
}
