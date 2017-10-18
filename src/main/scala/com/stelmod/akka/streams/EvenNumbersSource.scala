package com.stelmod.akka.streams

import akka.stream.scaladsl.Source

object EvenNumbersSource {
  def source(upper: Int) = Source(1 to upper).filter(_ % 2 == 0)
}
