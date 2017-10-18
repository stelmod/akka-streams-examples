package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class SourceTest extends WordSpec with Matchers with BeforeAndAfterAll {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Numbers Source with TestSink" in {
    val sourceUnderTest = NumbersSource.source(1, 4)

    sourceUnderTest
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(1, 2)
  }

  "Numbers Source with Seq" in {
    val sourceUnderTest = NumbersSource.source(1, 8)

    val future = sourceUnderTest.take(3).runWith(Sink.seq)
    val result = Await.result(future, 1 seconds)
    result should contain inOrder (1, 2, 3)
  }
}
