package com.adventofcode

import com.adventofcode.ElvenFilesystem.addDirectory
import com.adventofcode.ElvenFilesystem.addFile
import com.adventofcode.ElvenFilesystem.listAllNestedDirectories
import com.adventofcode.ElvenFilesystem.open
import com.adventofcode.ElvenFilesystem.openParent
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists

object ElvenFilesystem {

  private lateinit var pwd: Path
  private val dirToSize = mutableMapOf<String, Long>()
  private val dirToChild = mutableMapOf<String, MutableSet<String>>()

  fun open(directory: String) {
    if (isRoot()) {
      openRoot(directory)
      dirToChild[canonicalPath()] = mutableSetOf()
      return
    }
    openChild(directory)
    dirToChild[canonicalPath()] = mutableSetOf()
  }

  private fun openChild(directory: String) {
    val p = pwd / directory
    dirToChild[canonicalPath()]!!.add(canonicalPath(p))
    pwd /= directory
    makeSureExists(pwd)
  }

  private fun isRoot(): Boolean {
    return !::pwd.isInitialized
  }

  private fun openRoot(root: String) {
    require(root == "/")
    pwd = Path.of("root")
    if (pwd.exists()) {
      check(pwd.toFile().deleteRecursively())
    }
    makeSureExists(pwd)
  }

  fun addDirectory(directory: String) {
    val path = pwd / directory
    dirToChild[canonicalPath()]!!.add(canonicalPath(path))
    makeSureExists(pwd / directory)
  }

  private fun makeSureExists(directory: Path) {
    if (directory.notExists()) {
      directory.createDirectory()
    }
  }

  fun addFile(filename: String, size: Long) {
    updateSize(size)
    val path = pwd / filename
    val file = path.toFile()
    if (!file.exists()) {
      file.writeText("$size")
    }
  }

  private fun updateSize(size: Long) {
    val path = canonicalPath()
    dirToSize[path] = when (val prev = dirToSize[path]) {
      null -> size
      else -> prev + size
    }
  }

  private fun canonicalPath(): String {
    return canonicalPath(pwd)
  }

  private fun canonicalPath(p: Path): String {
    return p.toString().removePrefix("root").ifEmpty { "/" }
  }

  fun openParent() {
    pwd = pwd.parent
  }

  fun listAllNestedDirectories(): Map<String, Long> {
    val result = mutableMapOf<String, Long>()
    for((d, s) in dirToSize) {
      result[d] = s + dirToChild[d]!!.sumOf { dirToSize[it]!! }
    }
    return result
  }
}

fun execute(command: String) {
  if (command == "ls") {
    return // ls можно игнорировать
  }
  require(command.startsWith("cd "))
  val directory = command.drop(3)
  if (directory == "..") {
    openParent()
    return
  }
  open(directory)
}

fun parse(output: String) {
  val (marker, name) = output.split(' ')
  if (marker == "dir") {
    addDirectory(name)
    return
  }
  val size = marker.toLong()
  addFile(name, size)
}

fun process(line: String) {
  if (containsCommand(line)) {
    execute(commandOf(line))
    return
  }
  parse(line)
}

fun containsCommand(line: String): Boolean {
  return line.startsWith("$ ")
}

fun commandOf(line: String): String {
  return line.drop(2)
}

fun solution(): Long {
  return listAllNestedDirectories().values.filter { it <= 100000 }.sum()
}

fun main() {
  ::main.javaClass
    .getResourceAsStream("/input")!!
    .bufferedReader()
    .forEachLine(::process)
  println(listAllNestedDirectories())
  println(solution())
}
