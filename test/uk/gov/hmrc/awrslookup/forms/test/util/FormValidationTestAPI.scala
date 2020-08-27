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

package uk.gov.hmrc.awrslookup.forms.test.util

import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.awrslookup.forms.validation.util._


trait ImplicitSingleFieldTestAPI {

  implicit val fieldId: String

  def assertFieldIsCompulsory(config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsCompulsoryWhen(condition: Map[String, String], config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsCompulsoryWhen(conditions: Set[Map[String, String]], config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsOptional(config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsOptionalWhen(condition: Map[String, String], config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsOptionalWhen(conditions: Set[Map[String, String]], config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertEnumFieldIsCompulsory(config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertEnumFieldIsCompulsoryWhen(condition: Map[String, String], config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertEnumFieldIsCompulsoryWhen(conditions: Set[Map[String, String]], config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsIgnoredWhen(condition: Map[String, String], config: FieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIsIgnoredWhen(conditions: Set[Map[String, String]], config: FieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertEnumFieldIsIgnoredWhen(condition: Map[String, String], config: EnumFieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertEnumFieldIsIgnoredWhen(conditions: Set[Map[String, String]], config: EnumFieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit

}

trait ImplicitCrossFieldTestAPI {

  implicit val fieldIds: Set[String]

  def assertAtLeastOneFieldMustNotBeEmptyWhen(condition: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAtLeastOneFieldMustNotBeEmptyWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAllFieldsCannotBeAnsweredWithInvalidWhen(condition: Map[String, String], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAllFieldsCannotBeAnsweredWithInvalidWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAtLeastOneFieldMustNotBeEmptyIsIgnoredWhen(condition: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAtLeastOneFieldMustNotBeEmptyIsIgnoredWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAllFieldsCannotBeAnsweredWithInvalidIsIgnoredWhen(condition: Map[String, String], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertAllFieldsCannotBeAnsweredWithInvalidIsIgnoredWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit
}

trait FormValidationTestAPI {

  def assertErrorMessageIsCorrectlyPopulated(errorMessage: MessageLookup)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFormIsValid[T](form: Form[T], testData: Map[String, String])(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldError(formWithErrors: Form[_], fieldId: String, expected: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertDateFieldCannotBeEmpty(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, fieldIsEmptyExpectation: ExpectedFieldIsEmpty)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertNotThisFieldError(form: Form[_], fieldId: String, unacceptable: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertHasNoFieldError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertHasFieldError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertSummaryError(formWithErrors: Form[_], fieldId: String, expected: SummaryError)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertNotThisSummaryError(form: Form[_], fieldId: String, unacceptable: SummaryError)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertHasNoAnchorFromSummaryError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldCannotBeEmpty(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, fieldIsEmptyExpectation: ExpectedFieldIsEmpty)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldCannotBeExceedMaxLength(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, maxLengthExpectationOp: MaxLengthOption[ExpectedFieldExceedsMaxLength])(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldConformsExpectedFormats(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, formatExpectations: ExpectedFieldFormat)(implicit messages: Messages, messagesApi: MessagesApi): Unit


  def assertEnumFieldSatisfy(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value])(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIgnoresEmptyConstraintWhen(preCond: Map[String, String])(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIgnoresMaxLengthConstraintWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, maxLength: Int)(implicit messages: Messages, messagesApi: MessagesApi): Unit

  def assertFieldIgnoresFormatsConstraitsWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, formatExpectations: ExpectedFieldFormat)(implicit messages: Messages, messagesApi: MessagesApi): Unit


  def assertEnumFieldIgnoresConstraintsWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value])(implicit messages: Messages, messagesApi: MessagesApi): Unit
}

trait ExpectedErrorExpectation {
  def fieldError: FieldError

  def summaryError: SummaryError
}

case class ExpectedFieldIsEmpty(fieldError: FieldError, summaryError: SummaryError) extends ExpectedErrorExpectation

object ExpectedFieldIsEmpty {
  def apply(anchorId: String, fieldError: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): ExpectedFieldIsEmpty =
    new ExpectedFieldIsEmpty(fieldError, SummaryError(ErrorMessageInterpreter.defaultSummaryId(fieldError.msgKey), anchor = anchorId))
}

case class ExpectedFieldExceedsMaxLength(fieldError: FieldError, summaryError: SummaryError, maxLength: Int) extends ExpectedErrorExpectation

object ExpectedFieldExceedsMaxLength {
  // quick constructor for the default expected max length error messages
  def apply(fieldId: String, embeddedFieldNameInErrorMessages: String, maxLen: Int)(implicit messages: Messages, messagesApi: MessagesApi): ExpectedFieldExceedsMaxLength = {
    val defaultKey = "awrs.generic.error.maximum_length"
    val defaultError = FieldError(defaultKey, MessageArguments(embeddedFieldNameInErrorMessages, maxLen))
    new ExpectedFieldExceedsMaxLength(defaultError, SummaryError(ErrorMessageInterpreter.defaultSummaryId(defaultKey), MessageArguments(embeddedFieldNameInErrorMessages), fieldId), maxLen)
  }
}

sealed trait MaxLengthOption[+A] {
  def toOption = this match {
    case MaxLengthDefinition(maxLength) => Some(maxLength)
    case MaxLengthIsHandledByTheRegEx() => None
  }

  def nonEmpty: Boolean = this match {
    case MaxLengthDefinition(_) => true
    case MaxLengthIsHandledByTheRegEx() => false
  }

  def isEmpty: Boolean = this match {
    case MaxLengthDefinition(_) => false
    case MaxLengthIsHandledByTheRegEx() => true
  }
}

case class MaxLengthDefinition[A <: ExpectedFieldExceedsMaxLength](get: A) extends MaxLengthOption[A]

case class MaxLengthIsHandledByTheRegEx() extends MaxLengthOption[Nothing]

case class ExpectedInvalidFieldFormat(invalidCase: String, fieldError: FieldError, summaryError: SummaryError) extends ExpectedErrorExpectation

object ExpectedInvalidFieldFormat {
  def apply(invalidCase: String, fieldId: String, embeddedFieldNameInErrorMessages: String)(implicit messages: Messages, messagesApi: MessagesApi): ExpectedInvalidFieldFormat = {
    val defaultKey = "awrs.generic.error.character_invalid"
    val defaultFieldError = FieldError(defaultKey)
    val defaultSummaryError = SummaryError(ErrorMessageInterpreter.defaultSummaryId(defaultKey), MessageArguments(embeddedFieldNameInErrorMessages), fieldId)
    new ExpectedInvalidFieldFormat(invalidCase, defaultFieldError, defaultSummaryError)
  }

  def apply(invalidCase: String, fieldId: String, fieldError: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): ExpectedInvalidFieldFormat = {
    val defaultSummaryError = SummaryError(ErrorMessageInterpreter.defaultSummaryId(fieldError.msgKey), anchor = fieldId)
    new ExpectedInvalidFieldFormat(invalidCase, fieldError, defaultSummaryError)
  }
}

case class ExpectedValidFieldFormat(validCase: String)

case class ExpectedFieldFormat(invalidFormats: List[ExpectedInvalidFieldFormat], validFormats: List[ExpectedValidFieldFormat] = List[ExpectedValidFieldFormat]())

case class CompulsoryFieldValidationExpectations(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, maxLengthExpectation: MaxLengthOption[ExpectedFieldExceedsMaxLength], formatExpectations: ExpectedFieldFormat) {
  def toOptionalFieldValidationExpectations: OptionalFieldValidationExpectations = new OptionalFieldValidationExpectations(maxLengthExpectation, formatExpectations)

  def toFieldToIgnore: FieldToIgnore = FieldToIgnore(
    maxLengthExpectation match {
      case MaxLengthDefinition(maxLength) => Option(maxLength.maxLength)
      case _ => None
    }, formatExpectations)
}

object CompulsoryFieldValidationExpectations {
  def apply(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, maxLengthExpectation: ExpectedFieldExceedsMaxLength, formatExpectations: ExpectedFieldFormat) =
    new CompulsoryFieldValidationExpectations(fieldIsEmptyExpectation, MaxLengthDefinition(maxLengthExpectation), formatExpectations)
}

case class OptionalFieldValidationExpectations(maxLengthExpectation: MaxLengthOption[ExpectedFieldExceedsMaxLength], formatExpectations: ExpectedFieldFormat) {
  def toFieldToIgnore: FieldToIgnore = FieldToIgnore(
    maxLengthExpectation match {
      case MaxLengthDefinition(maxLength) => Option(maxLength.maxLength)
      case _ => None
    }, formatExpectations)
}

object OptionalFieldValidationExpectations {
  def apply(maxLengthExpectation: ExpectedFieldExceedsMaxLength, formatExpectations: ExpectedFieldFormat) =
    new OptionalFieldValidationExpectations(MaxLengthDefinition(maxLengthExpectation), formatExpectations)
}

case class CompulsoryEnumValidationExpectations(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value]) {
  def toIgnoreEnumFieldExpectation: EnumFieldToIgnore = new EnumFieldToIgnore(fieldIsEmptyExpectation, validEnumValues, invalidEnumValues)
}

object CompulsoryEnumValidationExpectations {
  private val empty: Set[Enumeration#Value] = Set[Enumeration#Value]()

  def apply(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, expectedEnum: Enumeration) = new CompulsoryEnumValidationExpectations(fieldIsEmptyExpectation, expectedEnum.values.toSet, empty)
}

case class FieldToIgnore(maxLength: Option[Int], formatExpectations: ExpectedFieldFormat)

case class EnumFieldToIgnore(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value])

object EnumFieldToIgnore {
  private val empty: Set[Enumeration#Value] = Set[Enumeration#Value]()

  def apply(fieldIsEmptyExpectation: ExpectedFieldIsEmpty, expectedEnum: Enumeration) = new EnumFieldToIgnore(fieldIsEmptyExpectation, expectedEnum.values.toSet, empty)
}

case class CrossFieldValidationExpectations(anchor: String, fieldIsEmptyExpectation: ExpectedFieldIsEmpty)


/**
  * A class used to specify prefix for ids
  *
  * It's intended to be used by FieldNameUtilAPI and ImplicitFieldNameUtil
  * to easily attach prefix to ids when it is abscent or supplied
  *
  * @param prefix
  */
case class IdPrefix(prefix: Option[String])

/**
  * implicit conversions from String and Option[String] to IdPrefix
  */
object IdPrefix {
  def apply(str: String): IdPrefix = new IdPrefix(Some(str))

  implicit def fromString(str: String): IdPrefix = new IdPrefix(Some(str))

  implicit def fromString(str: Option[String]): IdPrefix = new IdPrefix(str)
}

/**
  * function designed to be used by ImplicitFieldNameUtil
  * to allow easy attachment of prefix to field ids regardless of whether the
  * prefix is supplied
  */
trait FieldNameUtilAPI {
  def attach(fieldId: String): String

  def attachToAll(fieldIds: Set[String]): Set[String]
}
