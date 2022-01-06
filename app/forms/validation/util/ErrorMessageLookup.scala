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

package forms.validation.util

import play.api.i18n.{Messages, MessagesApi}

/**
  * This API is designed to look up messages from conf/messages using an instance of the MessageLookup returned by the
  * ErrorMessageInterpreter API.
  *
  * This call will also evaluate any EmbeddedMessages nested in the arguments.
  *
  * This API currently does not support different locales
  */
trait ErrorMessageLookup {
  def messageLookup(lookup: MessageLookup)(implicit messages: Messages, messagesApi: MessagesApi): String
}


object ErrorMessageLookup extends ErrorMessageLookup {

  @inline def messageLookup(lookup: MessageLookup)(implicit messages: Messages, messagesApi: MessagesApi): String = messageLookup(lookup.msgKey, lookup.msgArgs)

  private def messageLookup(key: String, params: MessageArguments)(implicit messages: Messages, messagesApi: MessagesApi): String =
    Messages(key, MessageArguments({
      for (param <- params.args) yield {
        param match {
          case EmbeddedMessage(x: String, args: MessageArguments) => messageLookup(x, args)
          case _ => param.toString
        }
      }
    }: _*).args: _*)
}
