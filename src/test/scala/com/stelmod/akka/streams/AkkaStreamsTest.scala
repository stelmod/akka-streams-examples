package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

class AkkaStreamsTest extends WordSpec with Matchers with BeforeAndAfter {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  before {
    system       = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Even numbers Source" in {
    val sourceUnderTest = Source(1 to 4).filter(_ % 2 == 0)

    sourceUnderTest
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(2, 4)
      .expectComplete()
  }
}
