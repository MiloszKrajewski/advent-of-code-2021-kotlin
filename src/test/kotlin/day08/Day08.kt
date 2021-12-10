package day08

import loadLines
import kotlin.test.Test

class Day08 {
    @Test
    fun part1() {
        val test = loadLines("day08/_test.txt")
        val testSolution = solve1(test)
        assert(testSolution == 26) { "found $testSolution unique digits" }

        val data = loadLines("day08/_data.txt")
        val dataSolution = solve1(data)
        println("part1: $dataSolution")
    }

    @Test
    fun part2() {
        val test = loadLines("day08/_test.txt")
        val testSolution = solve2("acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab | cdfeb fcadb cdfeb cdbaf")
        assert(testSolution == 5353) { "Test solution is not as expected" }

        val testSum = test.sumOf { solve2(it) }
        assert(testSum == 61229) { "Test sum is $testSum" }

        val data = loadLines("day08/_data.txt")
        val dataSum = data.sumOf { val r = solve2(it); println("$it -> $r"); r }
        println("Total sum: $dataSum")
    }

    private fun solve1(test: List<String>): Int {
        val lengths = test
            .map { l -> l.split('|')[1].trim() }
            .map { l -> l.split(' ') }
            .map { l -> l.map { i -> i.trim().length } }
        val combined = lengths.flatten()
        return combined.count { l -> l == 2 || l == 4 || l == 3 || l == 7 }
    }

    // be cfbegad cbdgef fgaecd cgeb fdcge agebfd fecdb fabcd edb | fdgacbe cefdb cefbgd gcbe

    private fun solve2(line: String): Int {
        val digits = line
            .split('|')[0]
            .trim()
            .split(' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toSet() }
            .toList()

        val solution = solve2(digits, emptyMap()) ?: throw Exception("Solution for '$line' could not found")

        var values = line
            .split('|')[1].trim()
            .split(' ')
            .map { it.trim().lowercase().toSet() }
            .map { replace(it, solution).sorted().joinToString("") }
            .map { map[it] ?: throw Exception("Digit '$it' could not be mapped") }

        return values.joinToString("") { it.toString() }.toInt()
    }

    private fun solve2(digits: List<Set<Char>>, mapped: Map<Char, Char>): Map<Char, Char>? {
        if (!mappingMakesSense(digits, mapped))
            return null

        val unmapped = all.lowercase().toSet().subtract(mapped.keys)

        if (unmapped.isEmpty())
            return mapped

        val candidate = unmapped.first()
        val available = all.uppercase().toSet().subtract(mapped.values.toSet())

        return available.firstNotNullOfOrNull { c -> solve2(digits, mapped.plus(Pair(candidate, c))) }
    }

    private fun mappingMakesSense(digits: List<Set<Char>>, mapped: Map<Char, Char>): Boolean =
        digits.all { matchesAny(replace(it, mapped)) }

    private fun replace(segments: Set<Char>, mapped: Map<Char, Char>): Set<Char> =
        segments.map { mapped[it] ?: it }.toSet()

    private fun matchesAny(segments: Set<Char>): Boolean =
        map.keys.any { allowed -> matchesOne(allowed, segments) }

    private fun matchesOne(allowed: String, segments: Set<Char>): Boolean =
        allowed.length == segments.size &&
                segments.filter { it.isUpperCase() }.all { allowed.contains(it) }

    /*
      0:      1:      2:      3:      4:
     aaaa    ....    aaaa    aaaa    ....
    b    c  .    c  .    c  .    c  b    c
    b    c  .    c  .    c  .    c  b    c
     ....    ....    dddd    dddd    dddd
    e    f  .    f  e    .  .    f  .    f
    e    f  .    f  e    .  .    f  .    f
     gggg    ....    gggg    gggg    ....

      5:      6:      7:      8:      9:
     aaaa    aaaa    aaaa    aaaa    aaaa
    b    .  b    .  .    c  b    c  b    c
    b    .  b    .  .    c  b    c  b    c
     dddd    dddd    ....    dddd    dddd
    .    f  e    f  .    f  e    f  .    f
    .    f  e    f  .    f  e    f  .    f
     gggg    gggg    ....    gggg    gggg
     */

    @Suppress("SpellCheckingInspection")
    private val all: String = "ABCDEFG"

    @Suppress("SpellCheckingInspection")
    private val map: Map<String, Int> = mapOf(
        "ABCEFG" to 0, "CF" to 1, "ACDEG" to 2, "ACDFG" to 3, "BCDF" to 4,
        "ABDFG" to 5, "ABDEFG" to 6, "ACF" to 7, "ABCDEFG" to 8, "ABCDFG" to 9
    )
}