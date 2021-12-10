package day03

import loadLines
import org.junit.jupiter.api.Test

class Day03 {
    data class Command(val Cmd: String, val Val: Int)

    @Test
    fun part1() {
        val (testBits, testRevs) = process_part1("day03/_test.txt")
        check(testBits == 22)
        check(testRevs == 9)
        check(testBits * testRevs == 198)

        val (dataBits, dataRevs) = process_part1("day03/_data.txt")
        println("${dataBits}*${dataRevs} = ${dataBits*dataRevs}")
    }

    @Test
    fun part2() {
        val (testO, testC) = processPart2("day03/_test.txt")
        check(testO == 23)
        check(testC == 10)
        check(testO * testC == 230)

        val (dataO, dataC) = processPart2("day03/_data.txt")
        println("${dataO}*${dataC} = ${dataO*dataC}")
    }

    private fun processPart2(fileName: String): Pair<Int, Int> {
        val test = loadLines(fileName)
        val ogr = findOxygenGeneratorRating(test)
        val csr = findCo2ScrubberRating(test)
        val ogrV = ogr.toInt(2)
        var csrV = csr.toInt(2)
        return Pair(ogrV, csrV)
    }

    private fun findOxygenGeneratorRating(lines: List<String>): String {
        val length = lines[0].length
        var acc = lines
        for (i in 0 until length) {
            if (acc.size == 1) return acc[0]
            var value = getMostFrequentBit(acc, i) ?: '1'
            acc = acc.filter { it[i] == value }
        }
        assert(acc.size == 1)
        return acc[0]
    }

    private fun findCo2ScrubberRating(lines: List<String>): String {
        val length = lines[0].length
        var acc = lines
        for (i in 0 until length) {
            if (acc.size == 1) return acc[0]
            var value = getLeastFrequentBit(acc, i) ?: '0'
            acc = acc.filter { it[i] == value }
        }
        assert(acc.size == 1)
        return acc[0]
    }

    private fun process_part1(fileName: String): Pair<Int, Int> {
        val test = loadLines(fileName)
        val bits = getMostFrequentBits(test)
        val revs = reverseBits(bits)
        val bitsV = bits.toInt(2)
        val revsV = revs.toInt(2)
        return Pair(bitsV, revsV)
    }

    private fun reverseBits(bits: String): String =
        bits.map { if (it == '0') '1' else '0' }.joinToString("")

    private fun getMostFrequentBits(test: List<String>) =
        (0 until test[0].length)
            .fold(StringBuilder()) { acc, i ->
                acc.append(getMostFrequentBit(test, i) ?: '0')
            }.toString()

    private fun getMostFrequentBit(lines: List<String>, bit: Int): Char? {
        val g = lines.groupBy { it[bit] }
        val f = g['0']?.size ?: 0
        val t = g['1']?.size ?: 1
        return when {
            f == t -> null
            f > t -> '0'
            else -> '1'
        }
    }

    private fun getLeastFrequentBit(lines: List<String>, bit: Int): Char? =
        when (getMostFrequentBit(lines, bit)) {
            null -> null
            '0' -> '1'
            '1' -> '0'
            else -> throw Exception("Neither null, 0 nor 1")
        }
}