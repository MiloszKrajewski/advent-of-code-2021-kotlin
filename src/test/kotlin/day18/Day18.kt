package day18

import failIfNull
import loadLines
import org.junit.jupiter.api.Test

class Day18 {
    sealed class Element {
        var parent: Couple? = null

        abstract fun depth(): Int
        abstract fun magnitude(): Long
        abstract fun clone(): Element

        class Number(var value: Int) : Element() {
            override fun depth(): Int = 0
            override fun magnitude(): Long = value.toLong()
            override fun toString(): String = value.toString()
            override fun clone(): Element = Number(value)
        }

        class Couple(var left: Element, var right: Element) : Element() {
            private var depth: Int? = null

            init {
                check(left.parent == null && right.parent == null)
                left.parent = this
                right.parent = this
                depth = depth()
            }

            override fun magnitude(): Long = left.magnitude() * 3 + right.magnitude() * 2

            override fun clone(): Element = Couple(left.clone(), right.clone())

            override fun depth(): Int {
                if (depth == null) depth = Integer.max(left.depth(), right.depth()) + 1
                return depth.failIfNull()
            }

            fun resetDepth() {
                depth = null
            }

            override fun toString(): String = "[$left,$right]"
        }
    }

    @Test
    fun test1_1() {
        val v1 = tryParseInt(Parser.create("123,"))
        check(v1!!.value == 123)

        val e1 = tryParseElement(Parser.create("[1,2]"))
        println(e1!!.value)

        val e2 = tryParseElement(Parser.create("[4,[[1,2],3]]"))
        println(e2!!.value)
    }

    @Test
    fun test1_2() {
        val v1 = loadElement("[1,2]")
        val v2 = loadElement("[[3,4],5]")
        check(v1.toString() == "[1,2]")
        check(v2.toString() == "[[3,4],5]")
        check(combine(v1, v2).toString() == "[[1,2],[[3,4],5]]")
    }

    @Test
    fun test1_deepDive() {
        val n1 = loadElement("[[[[[9,8],1],2],3],4]")
        check(leftmostBelow(n1, 4).toString() == "[9,8]")
        check(leftmostBelow(n1, 5).toString() == "9")
        check(leftmostBelow(n1, 6) == null)
    }

    @Test
    fun test1_findLeft() {
        val top = loadElement("[[[1,2],[3,4]],[5,6]]")
        check(findFirst(top).toString() == "1")
        check(findLast(top).toString() == "6")

        val e3 = navigate(top, "lrl")
        check(e3.toString() == "3")
        val e4 = navigate(e3, "pr")
        check(e4.toString() == "4")

        val e34 = e3.parent!!
        check(findLeft(e34).toString() == "2")
        check(findRight(e34).toString() == "5")

        val e56 = navigate(top, "rr").parent!!
        check(findLeft(e56).toString() == "4")
        check(findRight(e56) == null)

        val e12 = navigate(top, "lll").parent!!
        check(findLeft(e12) == null)
        check(findRight(e12).toString() == "3")
    }

    @Test
    fun test1_exploding() {
        test1_explode("[[[[[9,8],1],2],3],4]", "[[[[0,9],2],3],4]")
        test1_explode("[7,[6,[5,[4,[3,2]]]]]", "[7,[6,[5,[7,0]]]]")
        test1_explode("[[6,[5,[4,[3,2]]]],1]", "[[6,[5,[7,0]]],3]")
        test1_explode("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]", "[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]")
        test1_explode("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]", "[[3,[2,[8,0]]],[9,[5,[7,0]]]]")
    }

    private fun test1_explode(input: String, expected: String) {
        val e1 = loadElement(input)
        check(reduceExplode(e1) != null)
        check(e1.toString() == expected)
    }

    @Test
    fun test1_reduce_stepByStep() {
        fun parse(s: String) = loadElement(s)
        val v1 = parse("[[[[4,3],4],4],[7,[[8,4],9]]]")
        val v2 = parse("[1,1]")

        /*
        after addition: [[[[[4,3],4],4],[7,[[8,4],9]]],[1,1]]
        after explode:  [[[[0,7],4],[7,[[8,4],9]]],[1,1]]
        after explode:  [[[[0,7],4],[15,[0,13]]],[1,1]]
        after split:    [[[[0,7],4],[[7,8],[0,13]]],[1,1]]
        after split:    [[[[0,7],4],[[7,8],[0,[6,7]]]],[1,1]]
        after explode:  [[[[0,7],4],[[7,8],[6,0]]],[8,1]]
        */
        val s1 = combine(v1, v2)
        check(s1.toString() == "[[[[[4,3],4],4],[7,[[8,4],9]]],[1,1]]")
        val s2 = reduceExplode(s1).failIfNull()
        check(s2.toString() == "[[[[0,7],4],[7,[[8,4],9]]],[1,1]]")
        val s3 = reduceExplode(s2).failIfNull()
        check(s3.toString() == "[[[[0,7],4],[15,[0,13]]],[1,1]]")
        val s4 = reduceSplit(s3).failIfNull()
        check(s4.toString() == "[[[[0,7],4],[[7,8],[0,13]]],[1,1]]")
        val s5 = reduceSplit(s4).failIfNull()
        check(s5.toString() == "[[[[0,7],4],[[7,8],[0,[6,7]]]],[1,1]]")
        val s6 = reduceExplode(s5).failIfNull()
        check(s6.toString() == "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]")
    }

    @Test
    fun test1_reduce_allAtOnce() {
        fun parse(s: String) = loadElement(s)
        val v1 = parse("[[[[4,3],4],4],[7,[[8,4],9]]]")
        val v2 = parse("[1,1]")
        check(reduce(combine(v1, v2)).toString() == "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]")
    }

    @Test
    fun test1_mini1() {
        fun test(l: List<String>, s: String) = check(sum(l).toString() == s)
        test(listOf("[1,1]", "[2,2]", "[3,3]", "[4,4]"), "[[[[1,1],[2,2]],[3,3]],[4,4]]")
        test(listOf("[1,1]", "[2,2]", "[3,3]", "[4,4]", "[5,5]"), "[[[[3,0],[5,3]],[4,4]],[5,5]]")
        test(loadLines("day18/_test.txt"), "[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]")
    }

    @Test
    fun test1_magnitudes() {
        /*
        [[1,2],[[3,4],5]] becomes 143.
        [[[[0,7],4],[[7,8],[6,0]]],[8,1]] becomes 1384.
        [[[[1,1],[2,2]],[3,3]],[4,4]] becomes 445.
        [[[[3,0],[5,3]],[4,4]],[5,5]] becomes 791.
        [[[[5,0],[7,4]],[5,5]],[6,6]] becomes 1137.
        [[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]] becomes 3488.
        */
        fun test(s: String, m: Int) = check(loadElement(s).magnitude() == m.toLong())

        test("[[1,2],[[3,4],5]]", 143)
        test("[[[[0,7],4],[[7,8],[6,0]]],[8,1]]", 1384)
        test("[[[[1,1],[2,2]],[3,3]],[4,4]]", 445)
        test("[[[[3,0],[5,3]],[4,4]],[5,5]]", 791)
        test("[[[[5,0],[7,4]],[5,5]],[6,6]]", 1137)
        test("[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]", 3488)
    }

    @Test
    fun test1() {
        val combined = sum(loadLines("day18/_test2.txt"))
        check(combined.toString() == "[[[[6,6],[7,6]],[[7,7],[7,0]]],[[[7,7],[7,7]],[[7,8],[9,9]]]]")
        check(combined.magnitude() == 4140L)
    }

    @Test
    fun solve1() {
        val combined = sum(loadLines("day18/_data.txt"))
        println(combined.magnitude())
    }

    @Test
    fun test2() {
        val lines = loadLines("day18/_test2.txt")
        check(solve2(lines) == 3993L)
    }

    @Test
    fun data2() {
        val lines = loadLines("day18/_data.txt")
        solve2(lines)
    }

    private fun solve2(lines: List<String>): Long {
        val elements = lines.map { loadElement(it) }
        var maximum = 0L
        for (x in elements) {
            for (y in elements) {
                if (x == y) continue
                val current = reduce(combine(x.clone(), y.clone())).magnitude()
                if (current > maximum) maximum = current
            }
        }
        println("solve2: $maximum")
        return maximum
    }


    private fun navigate(top: Element, directions: String, index: Int = 0): Element {
        if (index >= directions.length) return top
        return when (directions[index]) {
            'l' -> navigate((top as Element.Couple).left, directions, index + 1)
            'r' -> navigate((top as Element.Couple).right, directions, index + 1)
            'p' -> navigate(top.parent.failIfNull(), directions, index + 1)
            else -> throw IllegalArgumentException("Bad directions!")
        }
    }

    private fun sum(lines: List<String>): Element =
        lines
            .map { loadElement(it) }
            .reduce { a, b -> reduce(combine(a, b)) }


    private fun reduce(top: Element): Element =
        generateSequence(top) { reduceExplode(it) ?: reduceSplit(it) }.last()

    private fun reduceExplode(top: Element): Element? {
        val leftmost = leftmostBelow(top, 4)
        return if (leftmost is Element.Couple) findTop(explode(leftmost)) else null
    }

    private fun reduceSplit(top: Element): Element? {
        return when (top) {
            is Element.Couple -> reduceSplit(top.left) ?: reduceSplit(top.right)
            is Element.Number -> {
                if (top.value < 10) null
                else {
                    val half = top.value / 2
                    val replacement = Element.Couple(
                        Element.Number(half),
                        Element.Number(top.value - half)
                    )
                    findTop(replace(top, replacement))
                }
            }
        }
    }

    private fun explode(couple: Element.Couple): Element {
        val l = couple.left as Element.Number
        val r = couple.right as Element.Number

        val ll = findLeft(couple)
        val rr = findRight(couple)

        if (ll != null) ll.value += l.value
        if (rr != null) rr.value += r.value

        val replacement = Element.Number(0)

        return replace(couple, replacement)
    }

    private fun <T : Element> replace(couple: Element, replacement: T): T {
        val parent = couple.parent

        if (isRightChild(couple)) {
            parent?.right = replacement
        } else if (isLeftChild(couple)) {
            parent?.left = replacement
        }
        replacement.parent = parent

        generateSequence(parent) { it.parent }.forEach { it.resetDepth() }

        return replacement
    }

    private fun findLeft(node: Element.Couple): Element.Number? {
        val parent = node.parent ?: return null
        if (isRightChild(node)) return findLast(parent.left)
        var pivot = generateSequence(parent) { it.parent }.firstOrNull { isRightChild(it) }?.parent
        return pivot?.let { findLast(it.left) }
    }

    private fun findRight(node: Element.Couple): Element.Number? {
        val parent = node.parent ?: return null
        if (isLeftChild(node)) return findFirst(parent.right)
        var pivot = generateSequence(parent) { it.parent }.firstOrNull { isLeftChild(it) }?.parent
        return pivot?.let { findFirst(it.right) }
    }

    private fun findTop(node: Element?): Element? {
        if (node == null) return null
        val parent = node.parent
        return if (parent == null) node else findTop(parent)
    }

    private fun isRightChild(node: Element): Boolean = node.parent?.let { node == it.right } ?: false
    private fun isLeftChild(node: Element): Boolean = node.parent?.let { node == it.left } ?: false

    private fun findFirst(a: Element): Element.Number =
        when (a) {
            is Element.Number -> a
            is Element.Couple -> findFirst(a.left)
        }

    private fun findLast(a: Element): Element.Number =
        when (a) {
            is Element.Number -> a
            is Element.Couple -> findLast(a.right)
        }

    private fun combine(a: Element, b: Element): Element.Couple = Element.Couple(a, b)

    private fun leftmostBelow(top: Element, depth: Int): Element.Couple? {
        if (top !is Element.Couple) return null
        if (depth == 0) return top
        if (top.depth() < depth) return null
        return leftmostBelow(top.left, depth - 1) ?: leftmostBelow(top.right, depth - 1)
    }

    private fun leftmost(top: Element): Element = when (top) {
        is Element.Number -> top
        is Element.Couple -> leftmost(top.left)
    }

    private fun loadElement(text: String): Element =
        tryParseElement(Parser.create(text)).failIfNull().value

    private fun tryParseElement(parser: Parser): Parsed<Element>? =
        tryParseNumber(parser) ?: tryParsePair(parser)

    private fun tryParsePair(parser: Parser): Parsed<Element.Couple>? {
        var state = parser

        val open = tryParseChr(state, '[') ?: return null
        state = open.parser

        val e1 = tryParseElement(state) ?: return null
        state = e1.parser

        val comma = tryParseChr(state, ',') ?: return null
        state = comma.parser

        val e2 = tryParseElement(state) ?: return null
        state = e2.parser

        val close = tryParseChr(state, ']') ?: return null
        state = close.parser

        return Parsed(Element.Couple(e1.value, e2.value), state)
    }

    private fun tryParseNumber(parser: Parser): Parsed<Element.Number>? =
        tryParseInt(parser)?.let { it.map { v -> Element.Number(v) } }

    private fun tryParseChr(parser: Parser, expected: Char): Parsed<Char>? {
        val found = parser.char(0)
        return if (found != expected) null else Parsed(found, parser.advance(1))
    }

    private fun tryParseInt(parser: Parser): Parsed<Int>? {
        var length = 0
        while (parser.char(length)?.isDigit() == true) length += 1
        return if (length == 0) null else Parsed(parser.head(length).toInt(), parser.advance(length))
    }

    data class Parser(val text: String, val offset: Int, val length: Int) {
        companion object {
            fun create(text: String) = Parser(text, 0, text.length)
        }

        fun advance(delta: Int): Parser =
            Parser(text, offset + delta, length - delta)

        fun char(idx: Int): Char? =
            if (idx < length) text[offset + idx] else null

        fun head(len: Int): String =
            text.substring(offset, offset + len.coerceAtMost(length))
    }

    data class Parsed<out T>(val value: T, val parser: Parser) {
        fun <U> map(func: (T) -> U) = Parsed(func(value), parser)
    }
}