package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable
import scala.concurrent.Future

class SinkTest extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "foreach sink" in {
    val source = NumbersSource.source(1, 3)
    val sink = Sink.foreach[Int](println)

    source.runWith(sink)
  }

  "seq sink with limit" in {
    val source = Source.single(1)
    val sink = Sink.seq[Int]
    val sinkResult: Future[immutable.Seq[Int]] = source.limit(1).runWith(sink)

    sinkResult.futureValue should contain only 1
  }

  "repeated seq sink from same source" in {
    val source = Source.single(1)
    val sink = Sink.seq[Int]

    val sinkResult: Future[immutable.Seq[Int]] = source.take(1).runWith(sink)
    sinkResult.futureValue should contain only 1

    val sinkResult2: Future[immutable.Seq[Int]] = source.take(1).runWith(sink)
    sinkResult2.futureValue should contain only 1

    val sinkResult3: Future[immutable.Seq[Int]] = source.take(1).runWith(Sink.seq[Int])
    sinkResult3.futureValue should contain only 1
  }

}
