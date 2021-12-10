package day06

import loadLines
import kotlin.test.Test

@Suppress("SameParameterValue")
class Day06 {
    val testText = "3,4,3,1,2"

    val dataText = ""

    @Test
    fun part1() {
        var test1 = combine(parseArray(testText))
        repeat(18) { test1 = step2(test1) }
        assert(test1.sum() == 26L)

        var test2 = combine(parseArray(testText))
        repeat(80) { test2 = step2(test2) }
        assert(test2.sum() == 5934L)

        var data1 = combine(parseArray(loadLines("day06/_data.txt").joinToString()))
        for (i in 0..80) {
            println("part1: $i -> ${data1.sum()}")
            data1 = step2(data1)
        }

        var data2 = combine(parseArray(loadLines("day06/_data.txt").joinToString()))
        for (i in 0..256) {
            println("part2: $i -> ${data2.sum()}")
            data2 = step2(data2)
        }
    }

    private fun step1(list: MutableList<Int>) {
        var counter = 0
        val length = list.size

        for (i in 0 until length) {
            val v = list[i]
            list[i] = if (v == 0) 6 else v - 1
            if (v == 0) counter += 1
        }

        for (i in 0 until counter) {
            list.add(8)
        }
    }

    private fun combine(fish: List<Int>): LongArray {
        val target = LongArray(32)
        for (f in fish) target[f] = target[f] + 1
        return target
    }

    private fun step2(source: LongArray): LongArray {
        val target = LongArray(32)
        target[8] += source[0]
        target[6] += source[0]
        for (i in 1 until source.size) {
            target[i - 1] += source[i]
        }
        return target
    }


    private fun parseArray(text: String) =
        text.split(',').map { it.toInt() }.toMutableList()
}