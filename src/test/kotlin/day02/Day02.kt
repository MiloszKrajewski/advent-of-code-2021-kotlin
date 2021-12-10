package day02

import loadLines
import org.junit.jupiter.api.Test

class Day02 {
    data class Command(val Cmd: String, val Val: Int)

    @Test
    fun part1() {
        val t = process1("day02/_test.txt")
        check(t.first == 15)
        check(t.second == 10)

        val p = process1("day02/_data.txt")
        print("part1: ${p.first * p.second}")
    }

    @Test
    fun part2() {
        val t = process2("day02/_test.txt")
        check(t.first == 15)
        check(t.second == 60)

        val p = process2("day02/_data.txt")
        print("part: ${p.first}*${p.second} = ${p.first * p.second}")
    }

    private fun process1(filename: String): Pair<Int, Int> {
        val lines = loadLines(filename)
        val commands = lines
            .map { it.split(' ', limit = 2) }
            .map { Command(it[0], it[1].toInt()) }

        var f = 0
        var d = 0

        for (c in commands) {
            when (c.Cmd) {
                "forward" -> f += c.Val
                "up" -> d -= c.Val
                "down" -> d += c.Val
                else -> {}
            }
        }

        return Pair(f, d)
    }

    private fun process2(filename: String): Pair<Int, Int> {
        val lines = loadLines(filename)
        val commands = lines
            .map { it.split(' ', limit = 2) }
            .map { Command(it[0], it[1].toInt()) }

        var f = 0
        var d = 0
        var a = 0

        for (c in commands) {
            when (c.Cmd) {
                "down" -> a += c.Val
                "up" -> a -= c.Val
                "forward" -> {
                    f += c.Val
                    d += a * c.Val
                }
                else -> {}
            }
        }

        return Pair(f, d)
    }
}