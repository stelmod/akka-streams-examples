package com.stelmod.akka.streams

import akka.stream.scaladsl.Flow

object Flows {
  def even = Flow[Int].filter(_ % 2 == 0)

  def largerThan(lowerLimit: Int) = Flow[Int].filter(_ > lowerLimit)
}
