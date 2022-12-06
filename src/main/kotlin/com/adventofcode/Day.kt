package com.adventofcode

import com.adventofcode.Day.process
import com.adventofcode.Day.solution

object Day {

  var solution = 0L; private set

  fun process(line: String) {
    
  }
}

fun main() {
  ::main.javaClass
    .getResourceAsStream("/input")!!
    .bufferedReader()
    .forEachLine(::process)
  println(solution)
}
