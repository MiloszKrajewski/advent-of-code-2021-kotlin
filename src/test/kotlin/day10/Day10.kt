package day10

import loadLines
import java.util.*
import kotlin.test.Test

class Day10 {
    private val opener = "([{<"
    private val closer = ")]}>"

    sealed class Result {
        object Valid: Result()
        data class Corrupted(val char: Char, val stack: String): Result()
        data class Incomplete(val stack: String): Result()
    }

    @Test
    fun part1() {
        check(validate("([])") == Result.Valid)
        check(validate("{()()()}") == Result.Valid)
        check(validate("<([{}])>") == Result.Valid)
        check(validate("[<>({}){}[([])<>]]") == Result.Valid)
        check(validate("(((((((((())))))))))") == Result.Valid)
        check(validate("(]") == Result.Corrupted(']', ")"))
        check(validate("(") == Result.Incomplete(")"))
        check(validate("([{<") == Result.Incomplete(">}])"))

        val test = loadLines("day10/_test.txt")
        check(score1(test) == 26397)

        val data = loadLines("day10/_data.txt")
        println("part1: ${score1(data)}")
    }

    @Test
    fun part2() {
        check(score2(validate("[({(<(())[]>[[{[]{<()<>>")) == 288957L)
        check(score2(validate("[(()[<>])]({[<{<<[]>>(")) == 5566L)
        check(score2(validate("(((({<>}<{<{<>}{[]{[]{}")) == 1480781L)
        check(score2(validate("{<[[]]>}<{[{[{[]{()[[[]")) == 995444L)
        check(score2(validate("<{([{{}}[<[[[<>{}]]]>[]]")) == 294L)

        val test = loadLines("day10/_test.txt")
        check(solve2(test) == 288957L)

        val data = loadLines("day10/_data.txt")
        println("part2: ${solve2(data)}")
    }

    private fun solve2(test: List<String>): Long {
        val testResults = test
            .map { score2(validate(it)) }
            .filter { it != 0L }
            .sorted()
            .toLongArray()
        return testResults[testResults.size / 2]
    }


    private fun score1(lines: List<String>): Int = lines.sumOf { score1(validate(it)) }
    private fun score1(result: Result): Int = when (result) {
        is Result.Corrupted -> score1(result.char)
        else -> 0
    }
    private fun score1(char: Char?): Int = when (char) {
        null -> 0
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> throw IllegalArgumentException("Invalid char $char")
    }

    private fun score2(result: Result): Long = when (result) {
        is Result.Incomplete -> result.stack.fold(0L) { acc, v -> acc * 5 + score2(v) }
        else -> 0
    }

    private fun score2(char: Char): Int = when (char) {
        ')' -> 1
        ']' -> 2
        '}' -> 3
        '>' -> 4
        else -> throw IllegalArgumentException("Invalid char $char")
    }

    private fun validate(line: String): Result {
        var index = 0
        val stack = Stack<Char>()
        while (index < line.length) {
            val c = line[index]
            if (opener.contains(c)) {
                stack.add(closer[opener.indexOf(c)])
            } else if (closer.contains(c)) {
                if (stack.empty() || c != stack.peek())
                    return Result.Corrupted(c, stack.flatten())
                stack.pop()
            } else {
                return Result.Corrupted(c, stack.flatten())
            }
            index += 1
        }
        return if (stack.empty()) Result.Valid else Result.Incomplete(stack.flatten())
    }
}

private fun Stack<Char>.flatten(): String = this.reversed().joinToString("")