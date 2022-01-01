package day19

import failIfNull
import loadLines
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.test.fail

class Day19 {
	data class Point(val x: Int, val y: Int, val z: Int) {
		companion object {
			val zero = Point(0, 0, 0)
		}

		fun vectorTo(other: Point): Vector = Vector(other.x - x, other.y - y, other.z - z)
		fun offset(vector: Vector): Point = Point(x + vector.x, y + vector.y, z + vector.z)
	}

	data class Rotation(val x: Int, val y: Int, val z: Int) {
		companion object {
			val zero = Rotation(0, 0, 0)
			val all = sequence {
				for (x in 0..3)
					for (y in 0..3)
						for (z in 0..3)
							yield(Rotation(x, y, z))
			}.toList()
		}
	}

	data class Transform(val rotation: Rotation, val offset: Vector) {
		companion object {
			val zero = Transform(Rotation.zero, Vector.zero)
		}
	}

	data class Vector(val x: Int, val y: Int, val z: Int) {
		companion object {
			val zero = Vector(0, 0, 0)
		}

		fun reverse(): Vector = Vector(-x, -y, -z)

		private fun rotateX(steps: Int): Vector = when (steps % 4) {
			0 -> this
			1 -> Vector(x, -z, y)
			2 -> Vector(x, -y, -z)
			3 -> Vector(x, z, -y)
			else -> fail("Invalid rotation")
		}

		private fun rotateY(steps: Int): Vector = when (steps % 4) {
			0 -> this
			1 -> Vector(-z, y, x)
			2 -> Vector(-x, y, -z)
			3 -> Vector(z, y, -x)
			else -> fail("Invalid rotation")
		}

		private fun rotateZ(steps: Int): Vector = when (steps % 4) {
			0 -> this
			1 -> Vector(y, -x, z)
			2 -> Vector(-x, -y, z)
			3 -> Vector(-y, x, z)
			else -> fail("Invalid rotation")
		}

		fun rotate(rotation: Rotation): Vector =
			rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z)

		fun mahattan(): Int = x.absoluteValue + y.absoluteValue + z.absoluteValue
	}

	class Scan(val center: Point, val vectors: Set<Vector>) {
		fun rotate(rotation: Rotation): Scan =
			Scan(center, vectors.map { it.rotate(rotation) }.toSet())

		fun offset(vector: Vector): Scan =
			Scan(center.offset(vector), vectors)

		fun transform(transform: Transform): Scan =
			rotate(transform.rotation).offset(transform.offset)

		fun match(point: Point): Boolean? {
			val diff = center.vectorTo(point)
			val maybe =
				diff.x in -1000..1000 &&
				diff.y in -1000..1000 &&
				diff.z in -1000..1000
			return if (!maybe) null else vectors.contains(diff)
		}

		fun match(other: Scan, max: Int = Int.MAX_VALUE): Int {
			var matches = 0
			for (o in other.points()) {
				val m = match(o) ?: continue
				if (!m) return 0
				matches += 1
				if (matches >= max) break
			}
			return matches
		}

		fun points(): List<Point> = vectors.map { v -> center.offset(v) }
	}

	fun findOffset(scanA: Scan, scanB: Scan): Vector? {
		for (pA in scanA.points()) {
			for (pB in scanB.points()) {
				val offset = pB.vectorTo(pA)
				val moved = scanB.offset(offset)
				val matches = scanA.match(moved, 12)
				if (matches >= 12) return offset
			}
		}
		return null
	}

	private fun findTransform(scanA: Scan, scanB: Scan): Transform? {
		for (r in Rotation.all) {
			val rotated = scanB.rotate(r)
			val offset = findOffset(scanA, rotated) ?: continue
			return Transform(r, offset)
		}
		return null
	}

	private fun loadReadings(lines: List<String>): List<Scan> {
		val result = mutableListOf<Scan>()
		var current: MutableList<Vector>? = null

		fun flush(vectors: List<Vector>?) {
			if (vectors != null && vectors.isNotEmpty())
				result.add(Scan(Point.zero, vectors.toSet()))
		}

		for (line in lines) {
			if (line.isBlank()) continue
			if (line.startsWith("---")) {
				flush(current)
				current = mutableListOf()
				continue
			}
			val vector = parseVector(line)
			current.failIfNull().add(vector)
		}
		flush(current)

		return result.toList()
	}

	private fun loadReadings(filename: String): List<Scan> = loadReadings(loadLines(filename))

	private fun parseVector(line: String): Vector {
		val coords = line.split(',').map { it.trim().toInt() }
		check(coords.size == 3)
		return Vector(coords[0], coords[1], coords[2])
	}

	fun solve1(scans: List<Scan>): List<Point> {
		return solve2(scans).flatMap { it.points() }.distinct()
	}

	private fun solve2(scans: List<Scan>): List<Scan> {
		var head = scans.take(1).toMutableList()
		var tail = scans.drop(1).toMutableList()

		outer@ while (tail.size > 0) {
			for (other in tail) {
				val transform = head.firstNotNullOfOrNull { s -> findTransform(s, other) }
				if (transform != null) {
					head.add(other.transform(transform))
					tail.remove(other)
					continue@outer
				}
			}
			fail("No solution found")
		}
		return head
	}

	@Test
	fun test1_1() {
		val scanners = loadReadings("day19/_test.txt")

		val scanner0 = scanners[0]

		val t01 = findTransform(scanners[0], scanners[1])
		check(t01 != null && t01.offset == Vector(68, -1246, -43))

		val scanner1 = scanners[1].transform(t01)

		val t14 = findTransform(scanner1, scanners[4])
		check(t14 != null)

		val scanner4 = scanners[4].transform(t14)
		check(scanner4.center == Point(-20, -1133, 1061))
	}

	@Test
	fun test1_2() {
		val scanners = loadReadings("day19/_test.txt")
		val solution = solve1(scanners)
		check(solution.size == 79)
	}

	@Test
	fun solve1() {
		val scanners = loadReadings("day19/_data.txt")
		val solution = solve1(scanners).size
		println("solve1: $solution")
	}

	@Test
	fun solve2() {
		val scanners = loadReadings("day19/_data.txt")
		val solution = solve2(scanners).map { it.center }

		var max = 0
		for (a in solution) {
			for (b in solution) {
				val dst = a.vectorTo(b).mahattan()
				if (dst > max) max = dst
			}
		}

		println("solve2: $max")
	}
}