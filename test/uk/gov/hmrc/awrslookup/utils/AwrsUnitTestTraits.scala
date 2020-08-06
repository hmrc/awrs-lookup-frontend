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

package uk.gov.hmrc.awrslookup.utils

import org.mockito.Matchers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.test.Helpers.{await => helperAwait, defaultAwaitTimeout}

import scala.concurrent.Future


trait AwrsUnitTestTraits extends PlaySpec with MockitoSugar with BeforeAndAfterEach with GuiceOneAppPerSuite {

  implicit lazy val hc = HeaderCarrier()

  implicit def convertToOption[T, U <: T](value: U): Option[T] = Some(value)

  implicit def convertToFuture[T](value: T): Future[Option[T]] = Future.successful(value)

  implicit def convertToFuture[T](err: Throwable): Future[Option[T]] = Future.failed(err)

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  implicit val environment: Environment = app.injector.instanceOf[Environment]

  implicit val configuration: Configuration = app.injector.instanceOf[Configuration]

  val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  // used to help mock setup functions to clarify if certain results should be mocked.
  sealed trait MockConfiguration[+A] {
    final def get = this match {
      case Configure(config) => config
      case _ => throw new RuntimeException("This element is not to be configured")
    }

    final def ifConfiguredThen(action: A => Unit): Unit = this match {
      case Configure(dataToReturn) => action(dataToReturn)
      case _ =>
    }
  }

  def await[A](result: Future[A]): A = {
    helperAwait(result)
  }

  case class Configure[A](config: A) extends MockConfiguration[A]

  case object DoNotConfigure extends MockConfiguration[Nothing]

  implicit def convertToMockConfiguration[T](value: T): MockConfiguration[T] = Configure(value)

  implicit def convertToMockConfiguration2[T](value: T): MockConfiguration[Option[T]] = Configure(value)

  implicit def convertToMockConfiguration3[T](value: T): MockConfiguration[Future[T]] = Configure(Future.successful(value))

  implicit def convertToMockConfiguration4[T](value: T): MockConfiguration[Future[Option[T]]] = Configure(Future.successful(Some(value)))

  implicit def convertToMockConfiguration5[T](err: Throwable): MockConfiguration[Future[Option[T]]] = Configure(err)

  sealed trait MatcherConfiguration[+A] {
    def matcher: A = this match {
      case AnyMatcher => Matchers.any()
      case EqMatcher(matchValue) => Matchers.eq(matchValue)
    }
  }

  case object AnyMatcher extends MatcherConfiguration[Nothing]

  case class EqMatcher[T](matchValue: T) extends MatcherConfiguration[T]

}
