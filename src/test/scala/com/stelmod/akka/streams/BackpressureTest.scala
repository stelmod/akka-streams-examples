package com.stelmod.akka.streams

import java.util.concurrent.TimeoutException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Attributes.InputBuffer
import akka.stream.scaladsl.{RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, DelayOverflowStrategy}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class BackpressureTest extends WordSpec with Matchers with BeforeAndAfterAll {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Delayed Flow" in {
    val source = NumbersSource.source(1, 10)
    val delayedSource: Source[Int, NotUsed] = source.via(Flows.even)
      .delay(1 seconds, DelayOverflowStrategy.backpressure)

    val future: Future[immutable.Seq[Int]] = delayedSource.take(10).runWith(Sink.seq)

    ScalaFutures.whenReady(future, Timeout(2 seconds)) {
      result => result should contain only (2, 4, 6, 8, 10)
    }
  }

  "Backpressure is visible when input buffer is small enough" in {
    val source = NumbersSource.source(1, 10)
    val flow = Flows.even

    val delayedSource: Source[Int, NotUsed] = source.via(flow)
      .delay(1 seconds, DelayOverflowStrategy.backpressure)
      .addAttributes(Attributes.inputBuffer(1, 1))

    val future: Future[immutable.Seq[Int]] = delayedSource.take(10).runWith(Sink.seq)

    an [TimeoutException] should be thrownBy Await.result(future, 2 seconds)
  }

  "Backpressure stops the source from producing" in {
    val source = NumbersSource.source(1, 10)
    val graph = Graphs.slowSink(source)

    val future = Source.fromGraph(graph).take(10).runWith(Sink.seq)
    an [TimeoutException] should be thrownBy Await.result(future, 25 seconds)
  }
}
