package day15

import loadLines
import org.junit.jupiter.api.Test

class Impl {
    data class Point(val x: Int, val y: Int) {
        fun move(dx: Int, dy: Int) = Point(dx + x, dy + y)
    }

    private val neighbours4 = listOf(Point(-1, 0), Point(1, 0), Point(0, -1), Point(0, 1))

    @Test
    fun mini1() {
        val map = loadMap("day15/_mini.txt")
        println("${solve1(map)}")
    }

    @Test
    fun test1() {
        val map = loadMap("day15/_test.txt")
        check(solve1(map) == 40)
    }

    @Test
    fun solve1() {
        val map = loadMap("day15/_data.txt")
        println("solve1: ${solve1(map)}")
    }

    @Test
    fun mini2() {
        val map0 = loadMap("day15/_mini.txt")
        debug(map0)
        val map1 = replicate(map0, 1, 0, 3)
        debug(map1)
        val map2 = replicate(map1, 0, 1, 2)
        debug(map2)
    }

    @Test
    fun test2() {
        val map1x1 = loadMap("day15/_test.txt")
        val map5x1 = replicate(map1x1, 1, 0, 5)
        val map5x5 = replicate(map5x1, 0, 1, 5)
        debug(map5x5)
        check(solve1(map5x5) == 315)
    }

    @Test
    fun solve2() {
        val map1x1 = loadMap("day15/_data.txt")
        val map5x1 = replicate(map1x1, 1, 0, 5)
        val map5x5 = replicate(map5x1, 0, 1, 5)
        val solution = solve1(map5x5)
        println("solve2: $solution")
    }

    private fun debug(map: Map<Point, Int>) {
        println("----")
        val w = map.keys.maxOf { it.x }
        val h = map.keys.maxOf { it.y }
        for (y in 0..h) {
            for (x in 0..w) {
                print(map[Point(x, y)])
            }
            println()
        }
    }

    private fun solve1(map: Map<Point, Int>): Int {
        val w = map.keys.maxOf { it.x }
        val h = map.keys.maxOf { it.y }
        val distances = dijkstra(Point(0, 0), map)
        return distances[Point(w, h)] ?: Int.MAX_VALUE
    }

    private fun replicate(map: Map<Point, Int>, dirX: Int, dirY: Int, c: Int): Map<Point, Int> {
        val result = mutableMapOf<Point, Int>()
        val w = map.keys.maxOf { it.x } + 1
        val h = map.keys.maxOf { it.y } + 1
        for (n in 0 until c) {
            for ((p, v) in map) {
                val x = dirX * w * n + p.x
                val y = dirY * h * n + p.y
                result[Point(x, y)] = (v - 1 + n) % 9 + 1
            }
        }
        return result
    }

    private fun dijkstra(point: Point, map: Map<Point, Int>): Map<Point, Int> {
        var current = point
        val visited = mutableSetOf<Point>()
        val ready = mutableSetOf<Point>()
        val distances = mutableMapOf<Point, Int>()
        distances[current] = 0

        while (true) {
            val total = distances[current] ?: Int.MAX_VALUE

            for (n in neighbours4.map { current.move(it.x, it.y) }) {
                if (visited.contains(n)) continue
                val cost = map[n] ?: continue
                val best = distances[n] ?: Int.MAX_VALUE
                if (total + cost < best) {
                    distances[n] = total + cost
                    ready.add(n)
                }
            }

            visited.add(current)
            ready.remove(current)

            current = (ready.minByOrNull { distances[it] ?: Int.MAX_VALUE }) ?: break
        }

        return distances
    }

    private fun loadMap(filename: String): Map<Point, Int> {
        val lines = loadLines(filename)
        val map = mutableMapOf<Point, Int>()
        for ((y, l) in lines.withIndex()) {
            for ((x, c) in l.withIndex()) {
                map[Point(x, y)] = c.digitToInt()
            }
        }
        return map
    }
}