package day11

import loadLines
import kotlin.test.Test

class Day11 {
    @Test
    fun part1() {
        val mini = loadMap("day11/_mini.txt")
        check(step1(mini) == 9)
        check(mini.debug() == "34543|40004|50005|40004|34543")
        debug(mini)
        check(step1(mini) == 0)
        debug(mini)

        val test = loadMap("day11/_test.txt")
        var total = 0
        for (i in 1..100) {
            val flashes = step1(test)
            total += flashes
            println("after $i: $flashes/$total")
            debug(test)

            checkWaypoint(i, total, test)
        }

        val data = loadMap("day11/_data.txt")
        val dataFlashes = (1..100).sumOf { step1(data) }
        println("part1: $dataFlashes")
    }

    @Test
    fun part2() {
        val test = loadMap("day11/_test.txt")
        check(solve2(test) == 195)

        val data = loadMap("day11/_data.txt")
        println("part2: ${solve2(data)}")
    }

    private fun solve2(map: OctoMap): Any {
        var steps = 0
        val size = map.width * map.height
        while (true) {
            val flashes = step1(map)
            steps += 1
            if (flashes == size) return steps
        }
    }

    private fun checkWaypoint(step: Int, flashes: Int, map: OctoMap) {
        fun checkImage(expected: String) = check(map.debug() == expected)
        if (step == 10) {
            check(flashes == 204)
            checkImage("0481112976|0031112009|0041112504|0081111406|0099111306|0093511233|0442361130|5532252350|0532250600|0032240000")
        }

        if (step == 20) {
            checkImage("3936556452|5686556806|4496555690|4448655580|4456865570|5680086577|7000009896|0000000344|6000000364|4600009543")
        }

        if (step == 100) {
            checkImage("0397666866|0749766918|0053976933|0004297822|0004229892|0053222877|0532222966|9322228966|7922286866|6789998766")
            check(flashes == 1656)
        }

    }

    private fun debug(mini: OctoMap) {
        mini.debug().split("|").forEach { println(it) }
        println("---")
    }

    fun step1(map: OctoMap): Int {
        map.points.forEach { map.inc(it) }
        val flashed = flashLoop(map)
        flashed.forEach { map.zero(it) }
        return flashed.size
    }

    private fun flashLoop(map: OctoMap, flashed: MutableSet<Point> = mutableSetOf()): Set<Point> {
        var counter = 0
        for (p in map.points) {
            if (flashed.contains(p)) continue
            if (map.get(p) > 9) {
                neighbours(map, p).forEach { map.inc(it) }
                flashed.add(p)
                counter += 1
            }
        }
        if (counter > 0) flashLoop(map, flashed)
        return flashed
    }

    private fun neighbours(map: OctoMap, p: Point): List<Point> =
        listOf(
            p.move(-1, -1), p.move(-1, 0), p.move(-1, 1),
            p.move(0, -1), p.move(0, 1),
            p.move(1, -1), p.move(1, 0), p.move(1, 1)
        ).filter { map.valid(it) }

    private fun loadMap(filename: String): OctoMap {
        val lines = loadLines(filename)
        val width = lines[0].length
        val height = lines.size
        val digits = lines.flatMap { l -> l.map { c -> c.digitToInt() } }.toIntArray()
        return OctoMap(height, width, digits)
    }

    data class Point(val y: Int, val x: Int) {
        fun move(dy: Int, dx: Int): Point = Point(y + dy, x + dx)
    }

    class OctoMap(
        val height: Int,
        val width: Int,
        private val state: IntArray
    ) {
        val points = buildPoints().toList()

        init {
            if (state.size != height * width)
                throw IllegalArgumentException("Invalid array size")

        }

        private fun index(p: Point) = p.y * width + p.x

        fun valid(p: Point): Boolean =
            p.y >= 0 && p.x >= 0 && p.y < height && p.x < width

        private fun buildPoints() = sequence {
            for (y in 0 until height)
                for (x in 0 until width)
                    yield(Point(y, x))
        }

        fun get(p: Point) = state[index(p)]

        fun set(p: Point, v: Int): Int {
            val index = index(p)
            state[index] = v
            return state[index]
        }

        fun inc(p: Point): Int {
            val index = index(p)
            state[index] += 1
            return state[index]
        }

        fun zero(p: Point) = set(p, 0)

        fun debug(): String =
            (0 until height).map { y ->
                (0 until width)
                    .map { x -> get(Point(y, x)) }
                    .map { v -> if (v > 9) '*' else v.toString() }
                    .joinToString("")
            }.joinToString("|")
    }
}

