package day01

import loadLines
import kotlin.test.Test

class Day01 {
    @Test
    fun part1() {
        // test if implementation meets criteria from the description, like:
        val test = loadLines("day01/_test.txt")
        val p1 = countIncreasing(test.map { Integer.parseInt(it) })

        check(p1 == 7)

        val data = loadLines("day01/_data.txt")
        val p2 = countIncreasing(data.map { Integer.parseInt(it) })

        print(p2)
    }

    @Test
    fun part2() {
        // test if implementation meets criteria from the description, like:
        val test = loadLines("day01/_test.txt")
        val p1 = countIncreasing(buildSliding(test.map { it.toInt() }))

        check(p1 == 5)

        val data = loadLines("day01/_data.txt")
        val p2 = countIncreasing(buildSliding(data.map { it.toInt() }))

        print(p2)
    }

    private fun countIncreasing(input: List<Int>): Int {
        var prev = 0
        var counter = 0
        for ((i, curr) in input.withIndex()) {
            if (i > 0 && curr > prev) {
                counter++
            }
            prev = curr
        }
        return counter
    }

    private fun buildSliding(input: List<Int>): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 0..input.size - 3) {
            val a = input[i]
            val b = input[i + 1]
            val c = input[i + 2]
            result.add(a + b + c)
        }
        return result
    }
}