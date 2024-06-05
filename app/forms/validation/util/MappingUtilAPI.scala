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

package forms.validation.util

import play.api.data.Forms._
import play.api.data.format._
import play.api.data.validation.{Invalid, Valid}
import play.api.data.{FieldMapping, FormError, Mapping}


object MappingUtilAPI {

  import ConstraintUtil._

  def compulsoryText(config: CompulsoryTextFieldMappingParameter): FieldMapping[Option[String]] = of(compulsoryTextFieldFormatter(config))

  def optionalText(config: OptionalTextFieldMappingParameter): FieldMapping[Option[String]] = of(optionalTextFieldMapping(config))

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
  def compulsoryTextFieldFormatter(config: CompulsoryTextFieldMappingParameter): Formatter[Option[String]] = new Formatter[Option[String]] {
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

  def optionalTextFieldMapping(config: OptionalTextFieldMappingParameter): Formatter[Option[String]] = new Formatter[Option[String]] {

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
