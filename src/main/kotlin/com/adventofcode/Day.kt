package com.adventofcode

import java.nio.file.Path
import kotlin.io.path.div

private lateinit var pwd: Path
private val dirToFiles = mutableMapOf<Path, Long>()
private val dirToNested = mutableMapOf<Path, MutableSet<Path>>()

fun processOutput(l: String) {
  val (marker, name) = l.split(' ')
  if (marker == "dir") {
    dirToNested.getOrPut(pwd, ::mutableSetOf).add(pwd / name)
    return
  }
  if (dirToFiles.contains(pwd)) {
    dirToFiles[pwd] = dirToFiles[pwd]!! + marker.toLong()
    return
  }
  dirToFiles[pwd] = marker.toLong()
}

fun processCode(l: String) {
  if (l == "ls") {
    return
  }
  require(l.startsWith("cd "))
  val dir = l.removePrefix("cd ")
  if (!::pwd.isInitialized) {
    pwd = Path.of(dir)
    return
  }
  if (dir == "..") {
    pwd = pwd.parent
    return
  }
  dirToNested.getOrPut(pwd, ::mutableSetOf).add(pwd / dir)
  pwd /= dir
}

fun process(l: String) {
  if (l.startsWith("$ ")) {
    processCode(l.removePrefix("$ "))
  } else {
    processOutput(l)
  }
}

fun calculateSize(dir: Path): Long {
  val filesSize = dirToFiles[dir] ?: 0
  val nestedDirs = dirToNested[dir] ?: emptySet()
  return nestedDirs.sumOf(::calculateSize) + filesSize
}

fun solution(): Long {
  val elapsed = calculateSize(Path.of("/"))
  val free = 70_000_000 - elapsed
  val lacks = 30_000_000 - free
  val allDirs = dirToFiles.keys + dirToNested.keys
  val candidatesToDelete = allDirs.map(::calculateSize).filter { it >= lacks }
  return candidatesToDelete.min()
}

fun main() {
  ::main.javaClass
    .getResourceAsStream("/input")!!
    .bufferedReader()
    .forEachLine(::process)
  println(solution())
}
