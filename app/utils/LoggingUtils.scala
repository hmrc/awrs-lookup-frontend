/*
 * Copyright 2025 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.Logging
import audit.Auditable
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

// This logging utility should be used to replace any manual logging or Splunk auditing
// This means that any Splunk audit calls will automatically be logged as DEBUG to aid local debugging but not appear in
// the production logs. All trace and debug calls will only appear locally so should only be used for local debugging
// and not for anything that you would want to see logged in production.
class LoggingUtils @Inject()(auditable: Auditable) extends Logging {

  final val auditLookupTxName: String = "AwrsLookup"

  final val eventTypeSuccess: String = "AwrsSuccess"
  final val eventTypeFailure: String = "AwrsFailure"
  final val eventTypeBadRequest: String = "BadRequest"
  final val eventTypeNotFound: String = "NotFound"
  final val eventTypeInternalServerError: String = "InternalServerError"
  final val eventTypeGeneric: String = "UnexpectedError"

  final val splunkString = "SPLUNK AUDIT:\n"

  private def splunkToLogger(transactionName: String, detail: Map[String, String], eventType: String): String =
    s"${if (eventType.nonEmpty) eventType + "\n"}$transactionName\n$detail"

  private def splunkFunction(transactionName: String, detail: Map[String, String], eventType: String)(implicit hc: HeaderCarrier,ec: ExecutionContext): Unit = {
    debug(splunkString + splunkToLogger(transactionName, detail, eventType))
    auditable.sendDataEvent(
      transactionName = transactionName,
      detail = detail,
      eventType = eventType
    )
  }

  def audit(transactionName: String, detail: Map[String, String], eventType: String)(implicit hc: HeaderCarrier,ec: ExecutionContext): Unit =
    splunkFunction(transactionName, detail, eventType)

  @inline def trace(msg: String): Unit = logger.trace(msg)

  @inline def trace(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit =
    trace(splunkToLogger(transactionName, detail, eventType))

  @inline def debug(msg: String): Unit = logger.debug(msg)

  @inline def debug(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit =
    debug(splunkToLogger(transactionName, detail, eventType))

  @inline def info(msg: String): Unit = logger.info(msg)

  @inline def info(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit =
    info(splunkToLogger(transactionName, detail, eventType))

  @inline def warn(msg: String): Unit = logger.warn(msg)

  @inline def warn(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit =
    warn(splunkToLogger(transactionName, detail, eventType))

  @inline def err(msg: String): Unit = logger.error(msg)

  @inline def err(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = err(splunkToLogger(transactionName, detail, eventType))

}
