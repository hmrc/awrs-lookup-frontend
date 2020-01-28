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

package uk.gov.hmrc.awrslookup.forms.validation.util

import play.api.data.Mapping
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}

object ConstraintUtil {

  type FormData = Map[String, String]

  object FormData {
    def apply(elems: scala.Tuple2[String, String]*): Map[String, String] = Map[String, String](elems: _*)
  }

  type FormQuery = FormData => Boolean

  implicit def castSingleToSet[A](value: A): Set[A] = Set(value)

  implicit def castToOptionPreCondition(preCondition: FormQuery): Option[FormQuery] = Some(preCondition)

  implicit class PostConstraintsUtil[A](c: Constraint[A]) {
    def andThen(c2: Constraint[A]): Constraint[A] =
      Constraint[A]({
        (a: A) =>
          c.apply(a) match {
            case Valid => c2(a)
            case f: Invalid => f
          }
      })

    def compose(c2: Constraint[A]): Constraint[A] =
      Constraint[A]({
        (a: A) =>
          c2.apply(a) match {
            case Valid => c(a)
            case f: Invalid => f
          }
      })
  }

  implicit class PreConstraintsUtil[A](cond: A => Boolean) {
    def preCondition(c: Constraint[A]): Constraint[A] =
      Constraint[A]({
        (a: A) =>
          cond(a) match {
            case true => c(a)
            case false => Valid
          }
      })
  }

  // used to combine (data) => conditions together
  implicit class PreconditionFunctionUtil(c: FormQuery) {
    def unary_! : FormQuery =
      (data: FormData) => !c(data)

    def `&&&`(c2: FormQuery): FormQuery =
      (data: FormData) => c(data) && c2(data)

    def `|||`(c2: FormQuery): FormQuery =
      (data: FormData) => c(data) || c2(data)
  }


  // always valid is required in case an empty sequence is passed in
  def alwaysValidConstraint[A]: Constraint[A] = Constraint[A]((a: A) => Valid)

  def andThenSeqChain[A](seq: Seq[Constraint[A]]) = seq.foldLeft(alwaysValidConstraint[A])(_ andThen _)

  def preConditionToConstraint[A](cond: A => Boolean, c: Constraint[A]): Constraint[A] =
    Constraint[A]({
      (a: A) =>
        cond(a) match {
          case true => c(a)
          case false => Valid
        }
    })

  implicit class MappingUtil[A](m: Mapping[A]) {
    def verifying(constraints: List[Constraint[A]]): Mapping[A] =
      constraints.foldLeft(m)(_.verifying(_))

    def verifying(constraints: Seq[Constraint[A]]): Mapping[A] =
      m.verifying(andThenSeqChain(constraints))

    def verifying(preCondition: Constraint[A], constraints: List[Constraint[A]]): Mapping[A] = {
      val newConsts = constraints.map(c => Constraint[A]("") {
        f =>
          preCondition.apply(f) match {
            case Valid => c(f)
            case f: Invalid => Valid
          }
      })
      m.verifying(preCondition).verifying(newConsts)
    }

    def verifying(preCondition: Constraint[A], constraints: Constraint[A]): Mapping[A] =
      verifying(preCondition, List(constraints))
  }

  // generic validation functions
  private val fieldIsNotEmptyValidationFunction = (field: String, invalid: ValidationResult) =>
    field.isEmpty match {
      case true => invalid
      case false => Valid
    }

  private val fieldMaxLengthValidationFunction = (field: String, maxLength: Int, invalid: ValidationResult) =>
    field.trim.length <= maxLength match {
      case true => Valid
      case false => invalid
    }

  private val fieldFormatValidationFunction = (field: String, patternMatch: (String) => ValidationResult) =>
    patternMatch(field)

  // configuration classes
  case class FieldIsEmptyConstraintParameter(errorMessage: Invalid)

  case class FieldMaxLengthConstraintParameter(len: Int, errorMessage: Invalid)

  case class FieldFormatConstraintParameter(patternMatch: (String) => ValidationResult)

  object FieldFormatConstraintParameter {
    implicit def castSingleToSeq(value: FieldFormatConstraintParameter): Seq[FieldFormatConstraintParameter] = Seq(value)
  }

  // used to apply on mappings
  trait ValidationMappingTrait[T]

  trait TextFieldMappingTrait[T] extends ValidationMappingTrait[T] {
    def maxLengthValidation: MaxLengthConstraintOption[FieldMaxLengthConstraintParameter]

    def formatValidations: Seq[FieldFormatConstraintParameter]
  }

  // custom Option[MaxLengthConstraintDefinition]
  sealed trait MaxLengthConstraintOption[+A] {
    def toOption = this match {
      case MaxLengthConstraintDefinition(maxLength) => Some(maxLength)
      case MaxLengthConstraintIsHandledByTheRegEx() => None
    }

    def nonEmpty: Boolean = this match {
      case MaxLengthConstraintDefinition(_) => true
      case MaxLengthConstraintIsHandledByTheRegEx() => false
    }

    def isEmpty: Boolean = this match {
      case MaxLengthConstraintDefinition(_) => false
      case MaxLengthConstraintIsHandledByTheRegEx() => true
    }
  }

  case class MaxLengthConstraintDefinition[A <: FieldMaxLengthConstraintParameter](get: A) extends MaxLengthConstraintOption[A]

  case class MaxLengthConstraintIsHandledByTheRegEx() extends MaxLengthConstraintOption[Nothing]

  implicit def castToOption(param: FieldMaxLengthConstraintParameter): MaxLengthConstraintOption[FieldMaxLengthConstraintParameter] = MaxLengthConstraintDefinition(param)

  case class CompulsoryTextFieldMappingParameter(empty: FieldIsEmptyConstraintParameter,
                                                 maxLengthValidation: MaxLengthConstraintOption[FieldMaxLengthConstraintParameter],
                                                 formatValidations: Seq[FieldFormatConstraintParameter]) extends TextFieldMappingTrait[String]

  case class OptionalTextFieldMappingParameter(maxLengthValidation: MaxLengthConstraintOption[FieldMaxLengthConstraintParameter],
                                               formatValidations: Seq[FieldFormatConstraintParameter]) extends TextFieldMappingTrait[Option[String]]

  case class CompulsoryListMappingParameter[T](mapping: Mapping[T],
                                               emptyErrorMsg: Invalid) extends ValidationMappingTrait[Mapping[T]]


  private def noConstraint[A] = Constraint[A] { ignore: A => Valid }

  // sub constraints for text fields
  private def fieldMustNotBeEmptyConstraint[A](config: FieldIsEmptyConstraintParameter): Constraint[A] =
  Constraint {
    case str: String =>
      fieldIsNotEmptyValidationFunction(str, config.errorMessage)
    case Some(str: String) =>
      fieldIsNotEmptyValidationFunction(str, config.errorMessage)
  }

  private def fieldMustBeWithinMaxLengthConstraint[A](config: MaxLengthConstraintOption[FieldMaxLengthConstraintParameter]): Constraint[A] =
    config match {
      case MaxLengthConstraintDefinition(maxLenConfig) =>
        Constraint {
          case str: String =>
            fieldMaxLengthValidationFunction(str, maxLenConfig.len, maxLenConfig.errorMessage)
          case Some(str: String) =>
            fieldMaxLengthValidationFunction(str, maxLenConfig.len, maxLenConfig.errorMessage)
        }
      case _ => noConstraint[A]
    }

  private def fieldMustHaveValidFormatConstraint[A](config: Seq[FieldFormatConstraintParameter]): Seq[Constraint[A]] =
    config.map(format => Constraint[A]("") {
      case str: String =>
        fieldFormatValidationFunction(str, format.patternMatch)
      case Some(str: String) =>
        fieldFormatValidationFunction(str, format.patternMatch)
    })

  // generic constraints for text fields on mappings
  def compulsaryTextFieldMappingConstraints(config: CompulsoryTextFieldMappingParameter): Constraint[String] =
  andThenSeqChain(
    Seq[Constraint[String]](
      fieldMustNotBeEmptyConstraint(config.empty),
      fieldMustBeWithinMaxLengthConstraint(config.maxLengthValidation),
      andThenSeqChain(fieldMustHaveValidFormatConstraint(config.formatValidations))
    )
  )

  def optionalTextFieldMappingConstraints(config: OptionalTextFieldMappingParameter): Constraint[Option[String]] =
    andThenSeqChain(
      Seq[Constraint[Option[String]]](
        fieldMustBeWithinMaxLengthConstraint(config.maxLengthValidation),
        andThenSeqChain(fieldMustHaveValidFormatConstraint(config.formatValidations))
      )
    )

  def compulsaryListConstraint[T](errMsgId: => Invalid): Constraint[List[T]] =
    Constraint[List[T]]({ model: List[T] =>
      model.nonEmpty && !model.contains("") match {
        case true => Valid
        case false => errMsgId
      }
    })
}
