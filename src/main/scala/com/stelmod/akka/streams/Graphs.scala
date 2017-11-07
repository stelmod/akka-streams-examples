package com.stelmod.akka.streams

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink, Source}
import scala.concurrent.duration._


object Graphs {
  def evenAndGreater(source: Source[Int, NotUsed], lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>
      val f2 = builder.add(Flows.largerThan(lowerLimit))

      source ~> Flows.even ~> f2

      SourceShape(f2.out)
    }
  }

  def greater(lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>

      val flow = builder.add(Flows.largerThan(lowerLimit))
      FlowShape(flow.in, flow.out)
    }
  }

  def slowSink(source: Source[Int, NotUsed]) = {
    GraphDSL.create() { implicit builder =>
      val flow = Flows.even.delay(10 seconds, DelayOverflowStrategy.backpressure).addAttributes(Attributes.inputBuffer(1, 1))
      val f2 = builder.add(flow)

      val broadcast = builder.add(Broadcast[Int](2))
      source ~> broadcast.in
      broadcast.out(0) ~> Sink.foreach[Int](println)
      broadcast.out(1) ~> f2.in

      SourceShape(f2.out)
    }
  }
}
