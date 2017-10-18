package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, DelayOverflowStrategy}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._

class DelayTest extends WordSpec with Matchers with BeforeAndAfterAll {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Delayed Flow" in {
    val source = NumbersSource.source(1, 4)
    val flow = Flows.even

    source.via(flow).delay(5 seconds, DelayOverflowStrategy.backpressure)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(10 seconds, 2)
      .expectNext(10 seconds, 4)
      .expectComplete()
  }
}
