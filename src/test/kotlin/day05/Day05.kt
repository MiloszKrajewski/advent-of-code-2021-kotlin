package day05

import loadLines
import kotlin.math.*
import kotlin.test.Test

class Day05 {
    data class Point(val x: Int, val y: Int)
    data class Line(val s: Point, val e: Point)

    @Test
    fun enumerateDiagonal() {
        println("${enumerate(0, 5, 0, 5)}")
        println("${enumerate(5, 1, 5, 1)}")
        println("${enumerate(5, 1, 5, 10)}")
    }

    @Test
    fun part1() {
        val testCount = part1("day05/_test.txt")
        assert(testCount == 5) { "count is $testCount" }

        val dataCount = part1("day05/_data.txt")
        println("data count is $dataCount")
    }

    @Test
    fun part2() {
        val testCount = part2("day05/_test.txt")
        assert(testCount == 12) { "count is $testCount" }

        val dataCount = part2("day05/_data.txt")
        println("data count is $dataCount")
    }


    private fun part1(fileName: String): Int {
        val test = loadLines(fileName)
        val map = buildMap(test, false)
        return findAbove(map, 2)
    }

    private fun part2(fileName: String): Int {
        val test = loadLines(fileName)
        val map = buildMap(test, true)
        return findAbove(map, 2)
    }


    private fun findAbove(map: Map<Point, Int>, limit: Int) =
        map.count { it.value >= limit }

    private fun buildMap(lines: List<String>, part2: Boolean): Map<Point, Int> {
        val map = mutableMapOf<Point, Int>()
        lines.forEach { updateMap(map, parseLine(it), part2) }
        return map
    }

    private fun updateMap(map: MutableMap<Point, Int>, coords: Line, part2: Boolean) {
        enumerate(coords, part2).forEach { map[it] = (map[it] ?: 0) + 1 }
    }

    private fun enumerate(line: Line, part2: Boolean): List<Point> {
        val (sx, sy) = line.s
        val (ex, ey) = line.e

        return if (sy == ey) {
            enumerate(sx, ex).map { Point(it, sy) }.toList()
        } else if (sx == ex) {
            enumerate(sy, ey).map { Point(sx, it) }.toList()
        } else if (part2) {
            enumerate(sx, ex, sy, ey)
        } else {
            emptyList()
        }
    }

    private fun enumerate(sx: Int, ex: Int, sy: Int, ey: Int): List<Point> {
        if (sx > ex)
            return enumerate(ex, sx, ey, sy)
        val yd = if (ey > sy) 1 else -1
        return (sx .. ex).map { Point(it, sy + (it - sx) * yd) }.toList()
    }

    private fun enumerate(a: Int, b: Int): IntRange =
        min(a, b) .. max(a, b)


    private fun parseLine(line: String): Line {
        val pair = line.split("->")
        return Line(parsePair(pair[0]), parsePair(pair[1]))
    }

    private fun parsePair(pair: String): Point {
        val number = pair.split(",")
        return Point(number[0].trim().toInt(), number[1].trim().toInt())
    }
}