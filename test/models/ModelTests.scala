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

import play.api.libs.json.Json
import models.AwrsStatus.{Approved, DeRegistered, Revoked}
import utils.AwrsUnitTestTraits

class ModelTests extends AwrsUnitTestTraits {

  val testInfo: Info = Info("testBusinessName", "testTradingName", "testFullName",
    Address("testline1", "testline2", "testline3", "testline4", "testPostCode", "testCountry"))

  "AwrsEntry" should {
    "Correctly convert Business to json and back with status Approved" in {
      val testObj: AwrsEntry = Business(
        awrsRef = "testValue",
        registrationDate = "01/01/1970",
        status = Approved,
        registrationEndDate = "01/01/2017",
        info = testInfo
      )
      val json = Json.toJson[AwrsEntry](testObj)

      val convBack = Json.fromJson[AwrsEntry](json)
      convBack.get mustBe testObj
    }

    "Correctly convert Business to json and back with status DeRegistered" in {
      val testObj: AwrsEntry = Business(
        awrsRef = "testValue",
        registrationDate = "01/01/1970",
        status = DeRegistered,
        registrationEndDate = "01/01/2017",
        info = testInfo
      )
      val json = Json.toJson[AwrsEntry](testObj)

      val convBack = Json.fromJson[AwrsEntry](json)
      convBack.get mustBe testObj
    }

    "Correctly convert Group to json and back with status Revoked" in {
      val testObj: AwrsEntry = Group(
        awrsRef = "testValue",
        registrationDate = "01/01/1970",
        status = Revoked,
        registrationEndDate = "01/01/2017",
        info = testInfo,
        members = List(
          testInfo.copy(businessName = "testBusinessName2"),
          testInfo.copy(businessName = "testBusinessName3")
        )
      )
      val json = Json.toJson[AwrsEntry](testObj)

      val convBack = Json.fromJson[AwrsEntry](json)
      convBack.get mustBe testObj
    }
  }

  "AwrsStatus" should {
    "Correctly convert an invalid status to -01" in {
      val json = Json.parse("""{
        | "code": "invalid status",
        | "name": "invalid name"
        }""".stripMargin)
      val model = Json.fromJson[AwrsStatus](json)
      model.get.code mustBe "-01"
    }
  }

}
