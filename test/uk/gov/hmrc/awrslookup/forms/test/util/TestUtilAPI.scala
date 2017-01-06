/*
 * Copyright 2017 HM Revenue & Customs
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

trait TestUtilAPI {

  /**
    * DataFormat is used to specify how the test data will be generated
    *
    * @param character specifies the pattern to use
    * @param repeat    specifies the number of times the pattern will be repeated
    */
  case class DataFormat(val character: Character, repeat: Int)

  object DataFormat {
    def apply(character: String, repeat: Int): DataFormat = {
      require(character.length == 1)
      new DataFormat(character.charAt(0), repeat)
    }
  }

  /**
    * generateTestDataWithFormat is used to aid the generation of test data for a text field
    *
    * It takes a sequence of DataFormat objects and combines their requirement into a single String
    *
    * Example usages:
    * generateTestDataWithFormat (DataFormat("a",10))
    * // will generate "aaaaaaaaaa"
    * generateTestDataWithFormat (DataFormat("A",2), DataFormat("1",6), DataFormat("A",1))
    * // will generate "AA111111A"
    *
    * @param formatRequirements a sequence of Dataformats for the string
    * @return a string consists of the specified Dataformats
    */
  def generateFieldTestDataInThisFormat(formatRequirements: DataFormat*): String = {
    require(formatRequirements.nonEmpty)
    formatRequirements.foldLeft("") {
      (x: String, format: DataFormat) =>
        x + format.character.toString * format.repeat
    }
  }

  /**
    * generateFormTestData is used to aid the generation of test data for a form
    *
    * In this overload a preCondition data map can be passed in as the base of the test data.
    * Mapping of the new data field will be appended to the preCondition map. This can then be bound to a form
    * which expects values from preCondition as well as a value from "fieldId"
    *
    * N.B. if the field already exists then the new value will overwrite the value that was in the
    * preCondition map
    *
    * Example usages:
    * val preCond = Map[String,String]("firstFieldId"->"value")
    * generateFormTestData (preCond,"newFieldId","newFieldValue")
    * // will generate:
    * // Map[String,String]("firstFieldId"->"value", "newFieldId" -> "newFieldValue")
    *
    * @param preCondition the data already exists for the form
    * @param fieldId      the id of new data field to be added to preCondition
    * @param dataValue    the value of the new data field
    * @return the new test data with (fieldId -> dataValue) appended to the preCondition
    */
  def generateFormTestData(preCondition: Map[String, String], fieldId: String, dataValue: String): Map[String, String] =
    preCondition ++ generateFormTestData(fieldId, dataValue)

  /**
    * generateFormTestData is used to aid the generation of test data for a form
    *
    * This overload will create a new Map[String,String] that can be bound to a form
    * which tests the value from a field with Id "fieldId"
    * *
    * Example usages:
    * generateFormTestData (newFieldId","newFieldValue")
    * // will generate:
    * // Map[String,String]("newFieldId" -> "newFieldValue")
    *
    * @param fieldId   the id of data field used in the test
    * @param dataValue the value of the new data field
    * @return the new test data Map(fieldId -> dataValue)
    */
  def generateFormTestData(fieldId: String, dataValue: String): Map[String, String] =
    Map(fieldId -> dataValue)


  /**
    * generateFormTestData is used to aid the generation of test data for a form
    *
    * This overload assign the data value on all of the field ids provided.
    * A preCondition data map can be passed in as the base of the test data
    * the new fields of the specified data value will be appended to the preCondition map.
    * This can then be bound to a form which expects values from preCondition as well as a value from the fieldIds
    *
    * N.B. if the field already exists then the new value will overwrite the value that was in the
    * preCondition map
    *
    * Example usages:
    * val preCond = Map[String,String]("firstFieldId"->"value")
    * generateFormTestData (preCond,Set("newFieldId1","newFieldId2","newFieldId3"),"newFieldValue")
    * // will generate:
    * // Map[String,String]("firstFieldId"->"value",
    * //                    "newFieldId1" -> "newFieldValue",
    * //                    "newFieldId2" -> "newFieldValue",
    * //                    "newFieldId3" -> "newFieldValue")
    *
    * @param preCondition the data already exists for the form
    * @param fieldIds     the ids of new data fields to be added to preCondition e.g. Set("fieldId1","fieldId1",...,"fieldIdN")
    * @param dataValue    the value to be set for all of the new data fields
    * @return the new test data with (fieldId1 -> dataValue) + (fieldId2 -> dataValue) ...(fieldIdN -> dataValue) appended to the preCondition
    */
  def generateFormTestData(preCondition: Map[String, String], fieldIds: Set[String], dataValue: String): Map[String, String] =
    fieldIds.foldLeft(preCondition)((map, fieldId) => map + (fieldId -> dataValue))


  /**
    * generateFormTestData is used to aid the generation of test data for a form
    *
    * This overload assign the data value on all of the field ids provided.
    * A preCondition data map can be passed in as the base of the test data
    * the new fields of the specified data value will be appended to the preCondition map.
    * This can then be bound to a form which expects values from preCondition as well as a value from the fieldIds
    *
    * N.B. if the field already exists then the new value will overwrite the value that was in the
    * preCondition map
    *
    * Example usages:
    * generateFormTestData (Set("newFieldId1","newFieldId2","newFieldId3"),"newFieldValue")
    * // will generate:
    * // Map[String,String]("newFieldId1" -> "newFieldValue",
    * //                    "newFieldId2" -> "newFieldValue",
    * //                    "newFieldId3" -> "newFieldValue")
    *
    * @param fieldIds  the ids of new data fields e.g. Set("fieldId1","fieldId1",...,"fieldIdN")
    * @param dataValue the value to be set for all of the new data fields
    * @return the new test data of Map(fieldId1 -> dataValue) + (fieldId2 -> dataValue) ...(fieldIdN -> dataValue)
    */
  def generateFormTestData(fieldIds: Set[String], dataValue: String): Map[String, String] =
    generateFormTestData(Map[String, String](), fieldIds, dataValue)

}
