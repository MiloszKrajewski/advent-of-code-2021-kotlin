package day20

import loadLines
import org.junit.jupiter.api.Test

class Day20 {
	data class Point(val x: Int, val y: Int) {
		fun offset(dx: Int, dy: Int) = Point(x + dx, y + dy)
	}

	class Image(val background: Boolean, private val bits: Set<Point>) {
		fun get(p: Point): Int = if (bits.contains(p) xor background) 1 else 0
		fun cnt(lit: Boolean = true): Long = if (lit == background) Long.MAX_VALUE else bits.size.toLong()
		fun poi(): Set<Point> = bits

		private fun update1(current: Int?, set: Boolean, mask: Int): Int {
			val result = current ?: (if (background) 511 else 0)
			return if (set) (result or mask) else ((result and mask.inv()) and 511)
		}

		private fun update9(p: Point, map: MutableMap<Point, Int>) {
			fun update(dx: Int, dy: Int, b: Int, v: Int) =
				map.compute(p.offset(-dx, -dy)) { _, x -> update1(x, v != 0, b) }

			val pixel = get(p)

			update(-1, -1, 256, pixel)
			update(0, -1, 128, pixel)
			update(1, -1, 64, pixel)

			update(-1, 0, 32, pixel)
			update(0, 0, 16, pixel)
			update(1, 0, 8, pixel)

			update(-1, 1, 4, pixel)
			update(0, 1, 2, pixel)
			update(1, 1, 1, pixel)
		}

		fun process(algorithm: Algorithm): Image {
			val map = mutableMapOf<Point, Int>()
			bits.forEach { update9(it, map) }

			val newBackground = if (algorithm.flip()) !this.background else this.background
			val nonBackground = map.mapNotNull { (p, v) -> if (algorithm.get(v) != newBackground) p else null }
			return Image(newBackground, nonBackground.toSet())
		}
	}

	class Algorithm(private val bits: Set<Int>) {
		fun valid(): Boolean = !get(0) || !get(511)
		fun flip(): Boolean = get(0)
		fun get(v: Int) = bits.contains(v)
	}

	fun load(filename: String): Pair<Algorithm, Image> {
		val lines = loadLines(filename)
		val algorithmBits = lines[0].withIndex().mapNotNull { (i, c) -> if (c == '#') i else null }.toSet()
		val imageBits = lines.drop(2).withIndex().flatMap { (row, l) ->
			l.withIndex().mapNotNull { (col, c) ->
				if (c == '#') Point(col, row) else null
			}
		}.toSet()
		val algorithm = Algorithm(algorithmBits)
		val image = Image(false, imageBits)
		check(algorithm.valid()) { "Algorithm is not valid" }
		return Pair(algorithm, image)
	}

	@Test
	fun test1_1() {
		val (algorithm, image) = load("day20/_test.txt")
		val twice = image.process(algorithm).process(algorithm)
		debug(twice)
		check(twice.cnt() == 35L)
	}

	@Test
	fun test1_2() {
		val (algorithm, i0) = load("day20/_debug.txt")
		debug(i0)
		val i1 = i0.process(algorithm)
		debug(i1)
		val i2 = i1.process(algorithm)
		debug(i2)
	}

	@Test
	fun solve1() {
		val (algorithm, image) = load("day20/_data.txt")
		val twice = image.process(algorithm).process(algorithm)
		println("solve1: ${twice.cnt()}")
	}

	@Test
	fun test2_1() {
		val (algorithm, image) = load("day20/_test.txt")
		val fiftyTimes = (1..50).fold(image) { acc, _ -> acc.process(algorithm) }
		check(fiftyTimes.cnt() == 3351L)
	}

	@Test
	fun solve2() {
		val (algorithm, image) = load("day20/_data.txt")
		val fiftyTimes = (1..50).fold(image) { acc, _ -> acc.process(algorithm) }
		println("solve2: ${fiftyTimes.cnt()}")
	}

	private fun debug(image: Image) {
		val points = image.poi()
		val minX = points.minOf { p -> p.x } - 1
		val minY = points.minOf { p -> p.y } - 1
		val maxX = points.maxOf { p -> p.x } + 1
		val maxY = points.maxOf { p -> p.y } + 1

		val sb = StringBuilder()
		for (y in minY..maxY) {
			for (x in minX..maxX) {
				val lit = image.get(Point(x, y)) != 0
				sb.append(if (lit) '#' else '.')
			}
			sb.appendLine()
		}

		println(sb.toString())
	}
}
