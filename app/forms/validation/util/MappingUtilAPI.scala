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

package forms.validation.util

import play.api.data.Forms._
import play.api.data.format._
import play.api.data.validation.{Invalid, Valid, ValidationResult}
import play.api.data.{FieldMapping, FormError, Mapping}


object MappingUtilAPI {

  import ConstraintUtil._

  def compulsoryText(config: CompulsoryTextFieldMappingParameter): FieldMapping[Option[String]] = of(CompulsoryTextFieldFormatter(config))

  def optionalText(config: OptionalTextFieldMappingParameter): FieldMapping[Option[String]] = of(OptionalTextFieldMapping(config))

  def compulsoryList[T](config: CompulsoryListMappingParameter[T]): Mapping[List[T]] =
    list(config.mapping).verifying(compulsaryListConstraint(config.emptyErrorMsg))

  implicit class MappingUtil(mapping: Mapping[Option[String]]) {
    /**
      * used to convert Option[String] formatters to String formatters, this so we can reuse the compulsory text
      * formatter on String fields instead of the default Option[String] fields
      */
    def toStringFormatter: Mapping[String] =
    mapping.transform[String]((value: Option[String]) => value.fold("")(x => x), (value: String) => Some(value))
  }

  implicit class MappingUtil_AddPreconditionToOptionMapping[T](mapping: Mapping[Option[T]]) {

    // attach the key to the mapping if they do not have the expected key
    private def checkKey(expectedKey: String, mapping: Mapping[Option[T]]): Mapping[Option[T]] = {
      val mappingKey = mapping.key
      expectedKey.equals(mappingKey) match {
        case true => mapping
        case false => //TODO need more tests to see if this can be simplified, could prob just append
          mapping.withPrefix(expectedKey)
      }
    }

    private val iffBind = (key: String, data: Map[String, String]) => (preCondition: Boolean) =>
      (preCondition match {
        case false => Right(None)
        case true =>
          val validated: Either[Seq[FormError], Option[T]] = checkKey(key, mapping).bind(data)
          validated match {
            case Left(errors) => Left(errors)
            case Right(ostring) => Right(ostring)
          }
      }): Either[Seq[FormError], Option[T]]

    private val iffUnbind = (key: String, value: Option[T]) => checkKey(key, mapping).unbind(value): Map[String, String]

    def iff(preCondition: Option[FormQuery]): Mapping[Option[T]] = of(new Formatter[Option[T]] {

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[T]] =
        iffBind(key, data)(preCondition.isEmpty || preCondition.get(data))

      def unbind(key: String, value: Option[T]): Map[String, String] =
        iffUnbind(key, value)

    })

    def iff(preCondition: FormQuery): Mapping[Option[T]] = of(new Formatter[Option[T]] {

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[T]] =
        iffBind(key, data)(preCondition.get(data))

      def unbind(key: String, value: Option[T]): Map[String, String] =
        iffUnbind(key, value)

    })

    def iff(preCondition: Boolean): Mapping[Option[T]] = of(new Formatter[Option[T]] {

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[T]] =
        iffBind(key, data)(preCondition)

      def unbind(key: String, value: Option[T]): Map[String, String] =
        iffUnbind(key, value)

    })

  }

  implicit class MappingUtil_AddPreconditionToMapping[T](mapping: Mapping[T]) {

    // attach the key to the mapping if they do not have the expected key
    private def checkKey(expectedKey: String, mapping: Mapping[T]): Mapping[T] = {
      val mappingKey = mapping.key
      expectedKey.equals(mappingKey) match {
        case true => mapping
        case false => //TODO need more tests to see if this can be simplified, could prob just append
          mapping.withPrefix(expectedKey)
      }
    }

    // used to attach a constraint to an existing mapping
    def `+`(additionalValidation: CrossFieldConstraint): Mapping[T] = of(new Formatter[T] {

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
        val first: Either[Seq[FormError], T] = checkKey(key, mapping).bind(data)

        val second: ValidationResult = additionalValidation.bind(data)

        def processErr(err: Invalid): Seq[FormError] =
          err.errors.toSeq.flatten.foldLeft(Seq[FormError]())((seq, ve) => seq ++ FormError(key, ve.message, ve.args))

        first match {
          case Left(ferrors) => Left(ferrors)
          case Right(fr) => second match {
            case inv: Invalid => Left(processErr(inv))
            case Valid => Right(fr)
          }
        }
      }

      def unbind(key: String, value: T): Map[String, String] =
        checkKey(key, mapping).unbind(value)
    })

  }

  class CrossFieldConstraint(val condition: FormQuery, val errorMessage: Invalid, val booleanValueOfInvalid: Boolean = true) {

    def bind(data: Map[String, String]) =
      condition(data) match {
        case `booleanValueOfInvalid` => errorMessage
        case _ => Valid
      }

    def xiff(preCondition: FormQuery): CrossFieldConstraint =
      new CrossFieldConstraint(condition, errorMessage, booleanValueOfInvalid) {
        override def bind(data: Map[String, String]) =
          preCondition(data) match {
            case true => super.bind(data)
            case false => Valid
          }
      }

  }

  object CrossFieldConstraint {
    def apply(condition: FormQuery, errorMessage: Invalid, booleanValueOfInvalid: Boolean = true) =
      new CrossFieldConstraint(condition, errorMessage, booleanValueOfInvalid)
  }

  def CompulsoryTextFieldFormatter(config: CompulsoryTextFieldMappingParameter) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val value = data.getOrElse(key, "").trim
      compulsaryTextFieldMappingConstraints(config)(value) match {
        case Valid => Right(Some(value))
        case e: Invalid => Left(e.errors.map(ve => FormError(key, ve.message, ve.args)))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  def OptionalTextFieldMapping(config: OptionalTextFieldMappingParameter) = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val value = data.getOrElse(key, "").trim
      value match {
        case "" => Right(None)
        case _ => optionalTextFieldMappingConstraints(config)(Some(value)) match {
          case Valid => Right(Some(value))
          case e: Invalid => Left(e.errors.map(ve => FormError(key, ve.message, ve.args)))
        }
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

}
