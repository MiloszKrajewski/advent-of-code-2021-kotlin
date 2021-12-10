package day10

import loadLines
import java.util.*
import kotlin.test.Test

class Day10 {
    @Test
    fun part1() {
        check(validate("([])").status == Status.Valid)
        check(validate("{()()()}").status == Status.Valid)
        check(validate("<([{}])>").status == Status.Valid)
        check(validate("[<>({}){}[([])<>]]").status == Status.Valid)
        check(validate("(((((((((())))))))))").status == Status.Valid)
        check(validate("(]") == Result(Status.Corrupted, ']', ")"))
        check(validate("(") == Result(Status.Incomplete, null, ")"))
        check(validate("([{<") == Result(Status.Incomplete, null, ">}])"))

        val test = loadLines("day10/_test.txt")
        check(score(test) { score1(it) } == 26397)

        val data = loadLines("day10/_data.txt")
        println("part1: ${score(data) { score1(it.char) }}")
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
        println(solve2(data))
    }

    private fun solve2(test: List<String>): Long {
        val testResults = test
            .map { score2(validate(it)) }
            .filter { it != 0L }
            .sorted()
            .toLongArray()
        return testResults[testResults.size / 2]
    }


    private fun score(lines: List<String>, scoreLine: (Result) -> Int): Int =
        lines.sumOf { scoreLine(validate(it)) }

    private fun score1(result: Result): Int = score1(result.char)

    private fun score1(char: Char?): Int = when (char) {
        null -> 0
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> throw IllegalArgumentException("Invalid char $char")
    }

    private fun score2(result: Result): Long =
        if (result.status != Status.Incomplete) 0
        else result.stack!!.fold(0L) { acc, v -> acc * 5 + score2(v) }

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
                    return Result(c, stack)
                stack.pop()
            } else {
                return Result(c, stack)
            }
            index += 1
        }
        return if (stack.empty()) Result() else Result(stack)
    }

    enum class Status { Valid, Incomplete, Corrupted }
    data class Result(
        val status: Status,
        val char: Char?,
        val stack: String?
    ) {
        constructor() : this(Status.Valid, null, null) {}
        constructor(char: Char, stack: Stack<Char>) : this(Status.Corrupted, char, stack.flatten())
        constructor(stack: Stack<Char>) : this(Status.Incomplete, null, stack.flatten())
    }

    private val opener = "([{<";
    private val closer = ")]}>"
}

private fun Stack<Char>?.flatten(): String? = this?.reversed()?.joinToString("")