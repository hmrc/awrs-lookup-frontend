/*
 * Copyright 2022 HM Revenue & Customs
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

/**
  * This implements the algorithm described in http://www.catalysoft.com/articles/StrikeAMatch.html,
  * accessed Dec 2016.
  */
object LetterPairSimilarity {

  /** @return lexical similarity value in the range [0,1] */
  def compareStrings(str1: String, str2: String): Double = {
    def coreAlgorithm = {
      val pairs1 = wordLetterPairs(str1.toUpperCase)
      val pairs2 = wordLetterPairs(str2.toUpperCase)

      val union = pairs1.size + pairs2.size
      val intersection = pairs1.intersect(pairs2).size

      (2.0 * intersection) / union
    }
    // this routine is added to deal with some of the edge cases not addressed by the original algorithm
    // e.g. if the search term is only a single letter or null string. However, these are unrealistic
    // scenarios in our usage
    (str1.length, str2.length) match {
      case (_, 0) => 0.0
      case (a, b) if a < b => compareStrings(str2, str1)
      case (a, 1) =>
        val intersection = str1.toUpperCase.toCharArray.intersect(str2.toUpperCase.toCharArray).size
        val union = a + 1
        (2.0 * intersection) / union
      case _ => coreAlgorithm
    }
  }

  /** @return an ArrayList of 2-character Strings. */
  private[utils] def wordLetterPairs(str: String): List[String] =
  str.split("\\s").map(letterPairs).reduce(_ ++ _)

  /** @return an array of adjacent letter pairs contained in the input string */
  private[utils] def letterPairs(str: String): List[String] = {
    str.length < 2 match {
      case true => List(str)
      case _ =>
        val ca = str.toCharArray
        ca.dropRight(1).zip(ca.drop(1)).toList.map {
          case (a, b) => s"$a$b"
        }
    }
  }

}
