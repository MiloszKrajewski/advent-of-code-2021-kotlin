package day07

import loadText
import kotlin.math.abs
import kotlin.test.Test

class Day07 {
    @Test
    fun part1() {
        val test = loadData("day07/_test.txt")
        solve1(test)
        solve2(test)

        val data = loadData("day07/_data.txt")
        solve1(data)
        solve2(data)
    }

    private fun solve1(test: List<Int>) {
        solve(test) { x -> x }
    }

    private fun solve2(test: List<Int>) {
        solve(test) { x -> x * (x + 1) / 2 }
    }

    private fun solve(test: List<Int>, cost: (Long) -> Long) {
        val min = test.minOrNull() ?: 0
        val max = test.maxOrNull() ?: 0

        val point = (min..max).minByOrNull { distanceFrom(it, test, cost) } ?: min

        println("$point -> ${distanceFrom(point, test, cost)}")
    }

    private fun distanceFrom(i: Int, test: List<Int>, cost: (Long) -> Long): Long =
        test.sumOf { cost(abs(it.toLong() - i)) }

    private fun loadData(filename: String): List<Int> =
        loadText(filename).split(',').map { it.trim().toInt() }.toList()
}