package com.stelmod.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class GraphTest extends WordSpec with Matchers with BeforeAndAfterAll {
  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll() {
    system = ActorSystem("actors")
    materializer = ActorMaterializer()
  }

  "graph with multiple flows" in {
    val source = NumbersSource.source(1, 20)

    val graph = Graphs.evenAndGreater(source, 7)
    Source.fromGraph(graph)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(8, 10)
  }

  "graphs can be composed" in {
    val source = NumbersSource.source(1, 20)

    val graph1: Graph[SourceShape[Int], NotUsed] = Graphs.evenAndGreater(source, 7)
    val graph2 = Graphs.greater(15)

    Source.fromGraph(graph1).via(graph2)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(16, 18)
  }
}
