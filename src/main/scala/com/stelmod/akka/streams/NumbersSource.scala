package com.stelmod.akka.streams

import akka.stream.scaladsl.Source

object NumbersSource {
  def source(from: Int, to: Int) = Source(from to to)
}
