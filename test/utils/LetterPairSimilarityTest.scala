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

package utils

class LetterPairSimilarityTest extends AwrsUnitTestTraits {

  "LetterPairSimilarity.letterPairs" should {
    "return an array of adjacent letter pairs contained in the input string" in {
      val test = "hello"
      val expected = List("he", "el", "ll", "lo")
      val actual = LetterPairSimilarity.letterPairs(test)
      actual mustBe expected

      val test2 = "a"
      val expected2 = List("a")
      val actual2 = LetterPairSimilarity.letterPairs(test2)
      actual2 mustBe expected2

      val test3 = ""
      val expected3 = List("")
      val actual3 = LetterPairSimilarity.letterPairs(test3)
      actual3 mustBe expected3
    }
  }

  "LetterPairSimilarity.wordLetterPairs" should {
    "return an ArrayList of 2-character Strings" in {
      val test = "hello world"
      val expected1 = LetterPairSimilarity.letterPairs("hello")
      val expected2 = LetterPairSimilarity.letterPairs("world")
      val actual = LetterPairSimilarity.wordLetterPairs(test)
      actual mustBe expected1 ++ expected2
    }
  }

  "LetterPairSimilarity.compareStrings" should {
    val epsilon = 1e-2f
    def test(str1: String, str2: String, expectedP: Double) = {
      val actual = LetterPairSimilarity.compareStrings(str1, str2)
      actual mustBe expectedP +- epsilon
    }

    "return 1 for an exact match" in {
      test("hello world", "hello world", 1.0)
    }

    // these test data are taken from: http://www.catalysoft.com/articles/StrikeAMatch.html, accessed Dec 2016
    "return lexical similarity value in the range [0,1]" in {
      test("FRANCE", "REPUBLIC OF FRANCE", 0.56)
      test("FRANCE", "QUEBEC", 0)
      test("FRENCH REPUBLIC", "REPUBLIC OF FRANCE", 0.72)
      test("FRENCH REPUBLIC", "REPUBLIC OF CUBA", 0.61)
    }

    "edge case handling" in {
      test("FRANCE", "", 0)
      test("FRANCE", "A", 2.0 / 7)
      test("FRANCE", "B", 0)
    }
  }

}
