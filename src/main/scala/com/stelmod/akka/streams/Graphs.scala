package com.stelmod.akka.streams

import akka.NotUsed
import akka.stream.{FlowShape, SourceShape}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{GraphDSL, Source}

object Graphs {
  def evenAndGreater(source: Source[Int, NotUsed], lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>

      val s = builder.add(source).out
      val f1 = builder.add(Flows.even)
      val f2 = builder.add(Flows.largerThan(lowerLimit))

      s ~> f1 ~> f2

      SourceShape.of(f2.out)
    }
  }

  def greater(lowerLimit: Int) = {
    GraphDSL.create() { implicit builder =>

      val flow = builder.add(Flows.largerThan(lowerLimit))
      FlowShape.of(flow.in, flow.out)
    }
  }
}
