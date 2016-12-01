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

package uk.gov.hmrc.awrslookup.controllers.util

import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future


trait AwrsLookupController extends FrontendController {
  implicit def optionUtil[T](data: T): Option[T] = Some(data)

  implicit def futureUtil[T](data: T): Future[T] = Future.successful(data)

  implicit def futureUtil[T](exception: Throwable): Future[T] = Future.failed(exception)
}
