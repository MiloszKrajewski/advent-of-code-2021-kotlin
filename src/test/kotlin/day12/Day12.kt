package day12

import loadLines
import kotlin.test.Test

class Day12 {
    @Test
    fun part1() {
        test1("day12/_test10.txt", 10)
        test1("day12/_test19.txt", 19)
        test1("day12/_test226.txt", 226)
        solve1("day12/_data.txt")
    }

    @Test
    fun part2() {
        test2("day12/_test10.txt", 36)
        test2("day12/_test19.txt", 103)
        test2("day12/_test226.txt", 3509)
        solve2("day12/_data.txt")
    }

    private fun test1(filename: String, expected: Int) {
        val test = loadMap(filename)
        val solutions = mutableListOf<List<String>>()
        findPaths1("start", emptyList(), emptySet(), test, solutions)
        solutions.forEach { println(it) }
        check(solutions.size == expected)
    }

    private fun test2(filename: String, expected: Int) {
        val test = loadMap(filename)
        val solutions = mutableListOf<List<String>>()
        findPaths2("start", emptyList(), emptySet(), false, test, solutions)
        solutions.forEach { println(it) }
        check(solutions.size == expected)
    }

    private fun solve1(filename: String) {
        val test = loadMap(filename)
        val solutions = mutableListOf<List<String>>()
        findPaths1("start", emptyList(), emptySet(), test, solutions)
        println("solve1: ${solutions.size}")
    }

    private fun solve2(filename: String) {
        val test = loadMap(filename)
        val solutions = mutableListOf<List<String>>()
        findPaths2("start", emptyList(), emptySet(), false, test, solutions)
        println("solve2: ${solutions.size}")
    }

    private fun loadMap(filename: String): CaveMap {
        val lines = loadLines(filename)
        val paths = lines.map { val parts = it.split('-'); Pair(parts[0], parts[1]) }
        return CaveMap(paths)
    }

    private fun findPaths1(
        current: String, history: List<String>,
        visited: Set<String>,
        map: CaveMap,
        solutions: MutableList<List<String>>
    ) {
        val path = history.plus(current)

        if (current == "end") {
            solutions.add(path)
            return
        }

        for (next in map.next(current)) {
            if (!map.large(next) && visited.contains(next)) continue
            findPaths1(next, path, visited.plus(current), map, solutions)
        }
    }

    private fun findPaths2(
        current: String, history: List<String>,
        visited: Set<String>, secondTryUsed: Boolean,
        map: CaveMap,
        solutions: MutableList<List<String>>
    ) {
        val path = history.plus(current)

        if (current == "end") {
            solutions.add(path)
            return
        }

        for (next in map.next(current)) {
            val startOrEnd = next == "end" || next == "start"
            val secondTry = !map.large(next) && visited.contains(next)
            if (secondTry && (secondTryUsed || startOrEnd)) continue
            findPaths2(next, path, visited.plus(current), secondTry || secondTryUsed, map, solutions)
        }
    }

    class CaveMap() {
        lateinit var map: Map<String, List<String>>
            private set

        constructor(paths: List<Pair<String, String>>) : this() {
            map = mutableMapOf<String, MutableList<String>>().apply {
                paths.forEach { (f, t) ->
                    getOrPut(f) { mutableListOf() }.add(t)
                    getOrPut(t) { mutableListOf() }.add(f)
                }
            }
        }

        fun next(current: String): List<String> = map[current] ?: emptyList()
        fun large(current: String): Boolean = current[0].isUpperCase()
    }
}