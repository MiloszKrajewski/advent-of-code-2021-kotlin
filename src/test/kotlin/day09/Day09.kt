package day09

import loadLines
import kotlin.test.Test

@Suppress("SameParameterValue")
class Day09 {
    @Test
    fun part1() {
        val test = loadMap("_test.txt")
        check(sumRisk(test, findLowPoints(test)) == 15)

        val data = loadMap("_data.txt")
        val dataSolution = sumRisk(data, findLowPoints(data))
        println("solution: $dataSolution")
    }

    @Test
    fun part2() {
        check(solve2("_test.txt") == 1134)
        println(solve2("_data.txt"))
    }

    private fun solve2(filename: String): Int {
        val test = loadMap(filename)
        val lowPoints = findLowPoints(test)
        return lowPoints
            .map { findBasinSize(test, it) }
            .sortedDescending().take(3)
            .fold(1) { acc, v -> acc * v }
    }

    private fun findBasinSize(data: Array<IntArray>, lp: Point): Int {
        val visited = mutableSetOf<Point>()
        findBasinSize(data, lp, visited)
        return visited.size
    }

    private fun findBasinSize(data: Array<IntArray>, lp: Point, visited: MutableSet<Point>) {
        visited.add(lp)
        for (n in neighboursOf(lp)) {
            if (visited.contains(n)) continue
            val nv = tryGetAt(data, n)
            if (nv == null || nv == 9) continue

            findBasinSize(data, n, visited)
        }
    }

    private fun neighboursOf(lp: Point): List<Point> = listOf(
        lp.move(-1, 0),
        lp.move(1, 0),
        lp.move(0, -1),
        lp.move(0, 1)
    )

    private fun sumRisk(test: Array<IntArray>, points: List<Point>): Int =
        points.sumOf { p -> test[p.y][p.x] + 1 }

    data class Point(val x: Int, val y: Int) {
        fun move(dx: Int, dy: Int): Point = Point(x + dx, y + dy)
    }

    private fun findLowPoints(data: Array<IntArray>): List<Point> {
        return sequence {
            for ((y, line) in data.withIndex()) {
                for ((x, digit) in line.withIndex()) {
                    val lower = neighboursOf(Point(x, y))
                        .map { p -> tryGetAt(data, p) ?: Int.MAX_VALUE }
                        .all { h -> h > digit }
                    if (lower)
                        yield(Point(x, y))
                }
            }
        }.toList()
    }

    private fun tryGetAt(ints: Array<IntArray>, p: Point): Int? =
        tryGetAt(ints, p.y, p.x)

    private fun tryGetAt(ints: Array<IntArray>, y: Int, x: Int): Int? =
        if (y < 0 || y >= ints.size) null else tryGetAt(ints[y], x)

    private fun tryGetAt(ints: IntArray, x: Int): Int? =
        if (x < 0 || x >= ints.size) null else ints[x]

    private fun loadMap(filename: String): Array<IntArray> =
        loadLines("day09/$filename").map { l -> l.map { c -> c.digitToInt() }.toIntArray() }.toTypedArray()
}