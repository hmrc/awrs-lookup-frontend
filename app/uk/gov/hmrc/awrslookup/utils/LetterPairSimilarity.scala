/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.utils

/**
  * This implements the algorithm described in http://www.catalysoft.com/articles/StrikeAMatch.html,
  * accessed Dec 2016.
  */
object LetterPairSimilarity {

  /** @return lexical similarity value in the range [0,1] */
  def compareStrings(str1: String, str2: String): Double = {
    val pairs1 = wordLetterPairs(str1.toUpperCase)
    val pairs2 = wordLetterPairs(str2.toUpperCase)

    val union = pairs1.size + pairs2.size
    val intersection = pairs1.intersect(pairs2).size

    (2.0 * intersection) / union
  }

  /** @return an ArrayList of 2-character Strings. */
  private def wordLetterPairs(str: String): List[String] =
  str.split("\\s").map(letterPairs).reduce(_ ++ _)

  /** @return an array of adjacent letter pairs contained in the input string */
  private def letterPairs(str: String): List[String] = {
    val ca = str.toCharArray
    ca.dropRight(1).zip(ca.drop(1)).toList.map {
      case (a, b) => s"$a$b"
    }
  }

}
