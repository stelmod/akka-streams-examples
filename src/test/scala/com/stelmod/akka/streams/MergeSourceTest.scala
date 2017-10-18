package com.stelmod.akka.streams

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source, Zip}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class MergeSourceTest extends WordSpec with Matchers with BeforeAndAfterAll {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "Zip Sources" in {
    val source1 = NumbersSource.source(1, 10)
    val source2 = NumbersSource.source(101, 110)

    val zipStream = GraphDSL.create() { implicit builder =>
      val zip = builder.add(Zip[Int, Int])
      val flow: FlowShape[(Int, Int), (Int, Int)] = builder.add(Flow[(Int, Int)].map(identity))

      source1 ~> zip.in0
      source2 ~> zip.in1
      zip.out ~> flow.in

      SourceShape.of(flow.out)
    }

      Source.fromGraph(zipStream)
        .runWith(TestSink.probe[(Int, Int)])
        .request(2)
        .expectNext((1, 101))
        .expectNext((2, 102))
  }

  "Merge Sources" in {
    val source1 = NumbersSource.source(1, 3)
    val source2 = NumbersSource.source(101, 103)

    val zipStream = GraphDSL.create() { implicit builder =>
      val m = builder.add(Merge[Int](2))
      source1 ~> m
      source2 ~> m

      SourceShape.of(m.out)
    }

    val future = Source.fromGraph(zipStream).take(10).runWith(Sink.seq)
    val result = Await.result(future, 1 seconds)
    result should contain theSameElementsAs Iterable(1, 2, 3, 101, 102, 103)
  }
}
