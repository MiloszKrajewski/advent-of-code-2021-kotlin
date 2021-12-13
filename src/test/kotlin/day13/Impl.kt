package day13

import loadLines
import org.junit.jupiter.api.Test

class Impl {
    @Test
    fun test1() {
        val map = loadMap("day13/_test.txt")
        debug(map)
        check(foldY(Point(0, 6), 7).y == 6)
        check(foldY(Point(0, 7), 7).y == 7)
        check(foldY(Point(0, 8), 7).y == 6)
        check(foldY(Point(0, 9), 7).y == 5)
        // ---
        val fold1 = foldY(map, 7)
        check(fold1.size == 17)
    }

    @Test
    fun part1() {
        val map = loadMap("day13/_data.txt")
        val step1 = foldX(map, 655)
        println("part1: ${step1.size}")
    }

    @Test
    fun part2() {
        val map = loadMap("day13/_data.txt")
        fun x(x: Int): (Set<Point>) -> Set<Point> = { foldX(it, x) }
        fun y(y: Int): (Set<Point>) -> Set<Point> = { foldY(it, y) }

        val transforms = listOf(
            x(655),
            y(447),
            x(327),
            y(223),
            x(163),
            y(111),
            x(81),
            y(55),
            x(40),
            y(27),
            y(13),
            y(6)
        )

        val solution = transforms.fold(map) { acc, f -> f(acc) }
        debug(solution)
    }

    private fun debug(map: Set<Point>) {
        val minX = map.minOfOrNull { it.x } ?: 0
        val maxX = map.maxOfOrNull { it.x } ?: 0
        val minY = map.minOfOrNull { it.y } ?: 0
        val maxY = map.maxOfOrNull { it.y } ?: 0

        println("----")
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                print(if (map.contains(Point(x, y))) '#' else '.')
            }
            println()
        }
    }

    private fun normalize(map: List<Point>): Set<Point> {
        // unsure if this step is needed as data seems to be friendly
        // but if after folding coordinates goes into negatives it
        // would mess with next fold line on same axis
        val minX = map.minOfOrNull { it.x } ?: 0
        val minY = map.minOfOrNull { it.y } ?: 0
        return map.map { Point(it.x - minX, it.y - minY) }.toSet()
    }

    private fun foldY(map: Set<Point>, limit: Int): Set<Point> =
        normalize(map.map { foldY(it, limit) })

    private fun foldX(map: Set<Point>, limit: Int): Set<Point> =
        normalize(map.map { foldX(it, limit) })

    private fun foldY(point: Point, limit: Int): Point =
        if (point.y <= limit) point else Point(point.x, limit - (point.y - limit))

    private fun foldX(point: Point, limit: Int): Point =
        if (point.x <= limit) point else Point(limit - (point.x - limit), point.y)

    private fun loadMap(filename: String): Set<Point> =
        loadLines(filename).map { parseLine(it) }.toSet()

    private fun parseLine(line: String): Point {
        val csv = line.split(',').map { it.trim().toInt() }
        assert(csv.size == 2)
        return Point(csv[0], csv[1])
    }

    data class Point(val x: Int, val y: Int)
}