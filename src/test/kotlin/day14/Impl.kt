package day14

import insertAt
import loadLines
import org.junit.jupiter.api.Test

class Impl {
    @Test
    fun test1() {
        val (state, rules) = loadData("day14/_test.txt")

        check(step1(state, rules, 1) == "NCNBCHB")
        check(step1(state, rules, 2) == "NBCCNBBBCBHCB")
        check(step1(state, rules, 3) == "NBBBCNCCNBBNBNBBCHBHHBCHB")
        check(step1(state, rules, 4) == "NBBNBNBBCCNBCNCCNBBNBBNBBBNBBNBBCBHCBHHNHCBBCBHCB")

        /*
        This polymer grows quickly. After step 5, it has length 97; After step 10, it has length 3073.
        After step 10, B occurs 1749 times, C occurs 298 times, H occurs 161 times, and N occurs 865 times;
        taking the quantity of the most common element (B, 1749) and subtracting the quantity of the least
        common element (H, 161) produces 1749 - 161 = 1588.
        */

        check(step1(state, rules, 5).length == 97)
        val after10 = step1(state, rules, 10)
        check(after10.length == 3073)
        check(countChar(after10, 'B') == 1749)
        check(countChar(after10, 'C') == 298)
        check(countChar(after10, 'H') == 161)
        check(countChar(after10, 'N') == 865)

        check(solve1(after10) == 1588)
    }

    @Test
    fun part1() {
        val (state, rules) = loadData("day14/_data.txt")
        val after10 = step1(state, rules, 10)
        val solution = solve1(after10)
        println("$solution")
    }

    @Test
    fun test2() {
        val (state, rules) = loadData("day14/_test.txt")
        val state2 = pairwise(state + "_")
        val after10 = step2(state2, rules, 10)
        check(solve2(after10) == 1588L)
        val after40 = step2(state2, rules, 40)
        check(solve2(after40) == 2188189693529L)
    }

    @Test
    fun part2() {
        val (state, rules) = loadData("day14/_data.txt")
        val state2 = pairwise(state + "_")
        val after40 = step2(state2, rules, 40)
        println("${solve2(after40)}")
    }

    private fun pairwise(state: String): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        for (i in 1 until state.length) {
            val sub = state.substring(i - 1..i)
            result[sub] = (result[sub] ?: 0) + 1
        }
        return result
    }

    private fun step2(state: Map<String, Long>, rules: List<Rule>, count: Int): Map<String, Long> =
        (0 until count).fold(state) { acc, _ -> step2(acc, rules) }

    private fun step2(state: Map<String, Long>, rules: List<Rule>): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        for (r in rules) {
            val found = (state[r.pair] ?: 0)
            if (found <= 0) continue
            val ab = r.pair
            val (a, b, c) = Triple(ab[0], ab[1], r.char)
            val ac = "$a$c"
            val cb = "$c$b"
            result[ab] = (result[ab] ?: 0) - found
            result[ac] = (result[ac] ?: 0) + found
            result[cb] = (result[cb] ?: 0) + found
        }
        for (kv in state) {
            result[kv.key] = (result[kv.key] ?: 0L) + kv.value
        }
        return result
    }

    private fun countChar(text: String, c: Char) = text.count { it == c }

    private fun solve1(text: String): Int {
        val counts = text.groupBy { it }.map { it.value.size }
        return (counts.maxOrNull() ?: 0) - (counts.minOrNull() ?: 0)
    }

    private fun solve2(pairs: Map<String, Long>): Long {
        val map = mutableMapOf<Char, Long>()
        for (p in pairs) {
            val c = p.key[0]
            map[c] = (map[c] ?: 0L) + p.value
        }
        return (map.values.maxOrNull() ?: 0L) - (map.values.minOrNull() ?: 0L)
    }


    private fun step1(state: String, rules: List<Rule>, count: Int): String =
        (0 until count).fold(state) { acc, _ -> step1(acc, rules) }

    private fun step1(state: String, rules: List<Rule>): String =
        rules
            .flatMap { r -> findAll(state, r.pair).map { i -> Pair(r.char, i) } }
            .sortedByDescending { (_, i) -> i }
            .fold(state) { acc, (c, i) -> acc.insertAt(i + 1, c) }

    private fun findAll(text: String, sub: String): List<Int> {
        val result = mutableListOf<Int>()
        var start = 0
        while (true) {
            val i = text.indexOf(sub, start)
            if (i < 0) break
            result.add(i)
            start = i + 1
        }
        return result
    }

    private fun loadData(filename: String): Pair<String, List<Rule>> {
        val lines = loadLines(filename)
        val state = lines[0]
        val rules = lines.drop(2).map { parseLine(it) }.toList()
        return Pair(state, rules)
    }

    private fun parseLine(it: String): Rule {
        val parts = it.replace(" -> ", "-").split('-').map { it.trim() }
        assert(parts.size == 2)
        assert(parts[1].length == 1)
        return Rule(parts[0], parts[1][0])
    }

    data class Rule(val pair: String, val char: Char)
}