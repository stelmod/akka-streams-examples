package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaStreamsTest extends WordSpec with Matchers with BeforeAndAfter {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  before {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Even numbers Source with TestSink" in {
    val sourceUnderTest = EvenNumbersSource.source(4)

    sourceUnderTest
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(2, 4)
      .expectComplete()
  }

  "Even numbers Source with Seq" in {
    val sourceUnderTest = EvenNumbersSource.source(4)

    val future = sourceUnderTest.take(2).runWith(Sink.seq)
    val result = Await.result(future, 1 seconds)
    result should contain inOrder (2, 4)
  }
}
