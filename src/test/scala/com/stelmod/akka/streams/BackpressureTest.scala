package com.stelmod.akka.streams

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeoutException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, DelayOverflowStrategy}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class BackpressureTest extends WordSpec with Matchers with BeforeAndAfterAll with BeforeAndAfter {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _
  var outputStream: ByteArrayOutputStream = _

  before {
    outputStream = new ByteArrayOutputStream
    Console.setOut(outputStream)
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Delayed Flow" in {
    val source = NumbersSource.source(1, 10)
    val delayedSource: Source[Int, NotUsed] = source.via(Flows.even)
      .delay(1 seconds, DelayOverflowStrategy.backpressure)

    val future: Future[immutable.Seq[Int]] = delayedSource.take(10).runWith(Sink.seq)

    // default buffer is 16, so take 10 gets all numbers without a 10 second delay
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

    // with buffer size 1, we only have time to read two numbers before the 2 sec timeout
    an [TimeoutException] should be thrownBy Await.result(future, 2 seconds)
  }

  "Backpressure stops the source from producing" in {
    val source = NumbersSource.source(1, 10)
    val graph = Graphs.filterAndDelayGraph(source)

    val future = Source.fromGraph(graph).take(10).runWith(Sink.seq)
    an [TimeoutException] should be thrownBy Await.result(future, 25 seconds)

    // 6 numbers should be printed, we delay only on even numbers
    outputStream.toString should be("1\n2\n3\n4\n5\n6\n")
  }

  "Backpressure stops the source from producing as early as the first delay" in {
    val source = NumbersSource.source(1, 10)
    val graph = Graphs.delayAndFilterGraph(source)

    val future = Source.fromGraph(graph).take(10).runWith(Sink.seq)
    an [TimeoutException] should be thrownBy Await.result(future, 25 seconds)

    // only 3 numbers should be printed, we delay on all numbers
    outputStream.toString should be("1\n2\n3\n")
  }
}
