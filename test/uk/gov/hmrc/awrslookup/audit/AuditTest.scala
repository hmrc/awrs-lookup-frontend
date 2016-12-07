package uk.gov.hmrc.awrslookup.audit

import java.util.concurrent.ConcurrentLinkedQueue

import uk.gov.hmrc.awrslookup.FrontendAuditConnector
import uk.gov.hmrc.play.audit.model.Audit._
import uk.gov.hmrc.play.audit.model.{Audit, AuditAsMagnet, DataEvent}
import uk.gov.hmrc.play.http.HeaderCarrier

class AuditTest() extends Audit("test", FrontendAuditConnector) {
  var capturedTxName: String = ""
  var capturedInputs: Map[String, String] = Map.empty
  private val dataEvents = new ConcurrentLinkedQueue[DataEvent]

  override def as[A](auditMagnet: AuditAsMagnet[A])(body: Body[A])(implicit hc: HeaderCarrier): A = {
    this.capturedTxName = auditMagnet.txName
    this.capturedInputs = auditMagnet.inputs
    super.as(auditMagnet)(body)
  }

  def capturedDataEvents: Seq[DataEvent] = dataEvents.toArray(new Array[DataEvent](0)).toSeq

  def captureDataEvent(event: DataEvent) = {
    this.dataEvents.add(event)
    ()
  }

  override def sendDataEvent: (DataEvent) => Unit = captureDataEvent
}
