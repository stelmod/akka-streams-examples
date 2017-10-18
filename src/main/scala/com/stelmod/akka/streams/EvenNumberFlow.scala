package com.stelmod.akka.streams

import akka.stream.scaladsl.Flow

object EvenNumberFlow {
  def even = Flow[Int].filter(_ % 2 == 0)
}
