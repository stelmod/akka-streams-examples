package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class FlowTest extends WordSpec with Matchers with BeforeAndAfterAll{
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Even number Flow" in {
    val source = NumbersSource.source(1, 4)
    val flow = EvenNumberFlow.even

    source.via(flow)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(2, 4)
      .expectComplete()
  }
}
