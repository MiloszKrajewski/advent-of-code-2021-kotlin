package day16

import loadText
import org.junit.jupiter.api.Test
import java.lang.Long.*

class Impl {
    sealed class Packet {
        data class Number(val version: Int, val value: Long) : Packet()
        data class Operator(val version: Int, val type: Int, val packets: List<Packet>) : Packet()
    }

    data class Parser(val text: String, val offset: Int) {
        fun advance(delta: Int) = Parser(text, offset + delta)
    }

    data class Result<out T>(val value: T, val parser: Parser) {
        fun <U> map(func: (T) -> U): Result<U> = Result(func(value), parser)
    }

    @Test
    fun testLoading() {
        check(loadMsg("D2FE28").text == "110100101111111000101000")
        check(loadMsg("38006F45291200").text == "00111000000000000110111101000101001010010001001000000000")
        check(loadMsg("EE00D40C823060").text == "11101110000000001101010000001100100000100011000001100000")
    }

    @Test
    fun testParsing1() {
        val state = loadMsg("D2FE28")
        val version = read3(state)
        val type = read3(version.parser)
        check(version.value == 6)
        check(type.value == 4)

        val number = readV(type.parser)
        check(number.value == 2021L)
    }

    @Test
    fun testSubPackets1() {
        val state = loadMsg("38006F45291200")
        val packet = readPacket(state)
        println(packet)
    }

    @Test
    fun testSubPackets2() {
        val state = loadMsg("EE00D40C823060")
        val packet = readPacket(state)
        println(packet)
    }

    @Test
    fun test1() {
        check(solve1(loadMsg("8A004A801A8002F478")) == 16)
        check(solve1(loadMsg("620080001611562C8802118E34")) == 12)
        check(solve1(loadMsg("C0015000016115A2E0802F182340")) == 23)
        check(solve1(loadMsg("A0016C880162017C3686B18A3D4780")) == 31)
    }

    @Test
    fun data1() {
        val data = loadMsg(loadText("day16/_data.txt"))
        val packet = readPacket(data).value
        println(packet)
        println("solve1: ${solve1(packet)}")
    }

    @Test
    fun test2() {
        check(eval(readPacket(loadMsg("C200B40A82")).value) == 3L)
        check(eval(readPacket(loadMsg("04005AC33890")).value) == 54L)
        check(eval(readPacket(loadMsg("880086C3E88112")).value) == 7L)
        check(eval(readPacket(loadMsg("CE00C43D881120")).value) == 9L)
        check(eval(readPacket(loadMsg("D8005AC2A8F0")).value) == 1L)
        check(eval(readPacket(loadMsg("F600BC2D8F")).value) == 0L)
        check(eval(readPacket(loadMsg("9C005AC2F8F0")).value) == 0L)
        check(eval(readPacket(loadMsg("9C0141080250320F1802104A08")).value) == 1L)
    }

    @Test
    fun data2() {
        val data = loadMsg(loadText("day16/_data.txt"))
        val packet = readPacket(data).value
        println("solve2: ${eval(packet)}")
    }

    private fun solve1(parser: Parser): Int {
        return solve1(readPacket(parser).value)
    }

    private fun solve1(packet: Packet): Int =
        when (packet) {
            is Packet.Number -> packet.version
            is Packet.Operator -> packet.version + packet.packets.sumOf { solve1(it) }
        }

    private fun eval(packet: Packet): Long =
        when (packet) {
            is Packet.Number -> packet.value
            is Packet.Operator -> when (packet.type) {
                0 -> packet.packets.fold(0) { acc, v -> acc + eval(v) }
                1 -> packet.packets.fold(1) { acc, v -> acc * eval(v) }
                2 -> packet.packets.fold(Long.MAX_VALUE) { acc, v -> min(acc, eval(v)) }
                3 -> packet.packets.fold(Long.MIN_VALUE) { acc, v -> max(acc, eval(v)) }
                5 -> if (eval(packet.packets[0]) > eval(packet.packets[1])) 1 else 0
                6 -> if (eval(packet.packets[0]) < eval(packet.packets[1])) 1 else 0
                7 -> if (eval(packet.packets[0]) == eval(packet.packets[1])) 1 else 0
                else -> throw IllegalArgumentException("Invalid packet type: ${packet.type}")
            }
        }

    private fun readPacket(parser0: Parser): Result<Packet> {
        val version = read3(parser0)
        val type = read3(version.parser)
        return when (type.value) {
            4 -> readNumber(version.value, type.parser)
            else -> readOperator(version.value, type)
        }
    }

    private fun tryReadPacket(parser0: Parser): Result<Packet>? =
        if (parser0.text.length - parser0.offset < 8) null else readPacket(parser0)

    private fun readNumber(version: Int, parser: Parser): Result<Packet.Number> =
        readV(parser).map { Packet.Number(version, it) }

    private fun readOperator(version: Int, type: Result<Int>): Result<Packet.Operator> {
        val mode = read1(type.parser)

        val packets = if (mode.value == 0) {
            readPacketsWithLength(readN(mode.parser, 15))
        } else {
            readPacketsWithCount(readN(mode.parser, 11))
        }

        return packets.map { Packet.Operator(version, type.value, it) }
    }

    private fun readPacketsWithCount(count: Result<Int>): Result<List<Packet>> {
        var left = count.value
        var parser = count.parser
        val result = mutableListOf<Packet>()
        while (left > 0) {
            val packet = readPacket(parser)
            result.add(packet.value)
            parser = packet.parser
            left -= 1
        }
        return Result(result, parser)
    }

    private fun readPacketsWithLength(length: Result<Int>): Result<List<Packet>> {
        val s = length.parser.offset
        val l = length.value
        val t = length.parser.text
        val parser = Parser(t.substring(s, s + l), 0)
        assert(parser.text.length == l)
        val packets = readPacketsUntilEnd(parser).value
        return Result(packets, length.parser.advance(l))
    }

    private fun readPacketsUntilEnd(parser0: Parser): Result<List<Packet>> {
        var parser = parser0
        val result = mutableListOf<Packet>()
        while (true) {
            val packet = tryReadPacket(parser) ?: return Result(result, parser)
            result.add(packet.value)
            parser = packet.parser
        }
    }


    private fun readV(parser: Parser): Result<Long> =
        readV(Result(0L, parser))

    private fun readV(state0: Result<Long>): Result<Long> {
        val (chunk5, parser) = read5(state0.parser)
        val next = (chunk5 and 0x10) != 0
        val bits = (chunk5 and 0x0F).toLong()
        val result = Result((state0.value shl 4) or bits, parser)
        return if (next) readV(result) else result
    }

    @Suppress("SameParameterValue")
    private fun <T> foldBits(
        state: T,
        text: String, offset: Int, length: Int,
        func: (T, Int) -> T
    ): T = text.substring(offset, offset + length).fold(state) { s, v -> func(s, if (v == '0') 0 else 1) }

    private fun read1(parser: Parser): Result<Int> = readN(parser, 1)
    private fun read3(parser: Parser): Result<Int> = readN(parser, 3)
    private fun read5(parser: Parser): Result<Int> = readN(parser, 5)

    private fun readN(parser: Parser, length: Int): Result<Int> =
        Result(
            foldBits(0, parser.text, parser.offset, length) { s, v -> (s shl 1) + v },
            parser.advance(length)
        )

    private fun loadMsg(message: String): Parser =
        Parser(message.fold("") { acc, c -> acc + hexToBits(c) }, 0)

    private fun hexToBits(hex: Char): String {
        val digit = hex.toString().toInt(16)
        fun bit(mask: Int) = if ((digit and mask) != 0) '1' else '0'
        return "${bit(0x08)}${bit(0x04)}${bit(0x02)}${bit(0x01)}"
    }
}