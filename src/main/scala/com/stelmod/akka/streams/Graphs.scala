package com.stelmod.akka.streams

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}

import scala.concurrent.duration._


object Graphs {
  def evenAndGreater(source: Source[Int, NotUsed], lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>
      val flow = builder.add(Flows.largerThan(lowerLimit))

      source ~> Flows.even ~> flow

      SourceShape(flow.out)
    }
  }

  def greater(lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>

      val flow = builder.add(Flows.largerThan(lowerLimit))
      FlowShape(flow.in, flow.out)
    }
  }

  def filterAndDelayGraph(source: Source[Int, NotUsed]) = {
    GraphDSL.create() { implicit builder =>
      val filterWithDelay = Flows.even
        .delay(10 seconds, DelayOverflowStrategy.backpressure)
        .addAttributes(Attributes.inputBuffer(1, 1))

      val flow = builder.add(filterWithDelay)

      val broadcast = builder.add(Broadcast[Int](2))
      source ~> broadcast.in
      broadcast.out(0) ~> Sink.foreach[Int](println)
      broadcast.out(1) ~> flow.in

      SourceShape(flow.out)
    }
  }

  def delayAndFilterGraph(source: Source[Int, NotUsed]) = {
    GraphDSL.create() { implicit builder =>
      val filterWithDelay = Flow[Int].delay(10 seconds, DelayOverflowStrategy.backpressure)
        .addAttributes(Attributes.inputBuffer(1, 1))
        .via(Flows.even)

      val flow = builder.add(filterWithDelay)

      val broadcast = builder.add(Broadcast[Int](2))
      source ~> broadcast.in
      broadcast.out(0) ~> Sink.foreach[Int](println)
      broadcast.out(1) ~> flow.in

      SourceShape(flow.out)
    }
  }
}
