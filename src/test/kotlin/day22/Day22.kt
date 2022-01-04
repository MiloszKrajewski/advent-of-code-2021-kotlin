package day22

import failIfNull
import loadLines
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import overlap

class Day22 {
	data class Point(val x: Int, val y: Int, val z: Int)
	data class Cube(val a: Point, val b: Point) {
		companion object {
			fun create(x: IntRange, y: IntRange, z: IntRange): Cube = Cube(
				Point(x.first, y.first, z.first),
				Point(x.last, y.last, z.last)
			).fix()
		}

		fun fix(): Cube = Cube(
			Point(Integer.min(a.x, b.x), Integer.min(a.y, b.y), Integer.min(a.z, b.z)),
			Point(Integer.max(a.x, b.x), Integer.max(a.y, b.y), Integer.max(a.z, b.z))
		)

		fun contains(p: Point): Boolean =
			p.x >= a.x && p.x <= b.x &&
			p.y >= a.y && p.y <= b.y &&
			p.z >= a.z && p.z <= b.z

		fun inside(bounds: Cube): Boolean =
			bounds.contains(a) && bounds.contains(b)

		fun points(): Sequence<Point> {
			return sequence {
				for (z in a.z..b.z) {
					for (y in a.y..b.y) {
						for (x in a.x..b.x) {
							yield(Point(x, y, z))
						}
					}
				}
			}
		}

		val volume: Long
			get() {
				val dx = b.x - a.x + 1
				val dy = b.y - a.y + 1
				val dz = b.z - a.z + 1
				return dx.toLong() * dy.toLong() * dz.toLong()
			}

		fun overlap(other: Cube): Cube? {
			val x = (this.a.x..this.b.x).overlap(other.a.x..other.b.x)
			val y = (this.a.y..this.b.y).overlap(other.a.y..other.b.y)
			val z = (this.a.z..this.b.z).overlap(other.a.z..other.b.z)
			if (x == null || y == null || z == null) return null
			return Cube(Point(x.first, y.first, z.first), Point(x.last, y.last, z.last))
		}
	}

	data class Command(val turnOn: Boolean, val cube: Cube)

	class History(private val commands: List<Command>) {
		fun check(p: Point): Boolean {
			for (i in commands.size - 1 downTo 0) {
				val command = commands[i]
				if (command.cube.contains(p)) return command.turnOn
			}
			return false
		}

		fun partition(): Sequence<Cube> {
			fun milestones(strategy: (Cube) -> List<Int>): List<Int> =
				commands.flatMap { strategy(it.cube) }.distinct().sorted()

			val xs = milestones { listOf(it.a.x, it.b.x + 1) }
			val ys = milestones { listOf(it.a.y, it.b.y + 1) }
			val zs = milestones { listOf(it.a.z, it.b.z + 1) }

			return sequence {
				for (ix in 1 until xs.size) {
					for (iy in 1 until ys.size) {
						for (iz in 1 until zs.size) {
							val a = Point(xs[ix - 1], ys[iy - 1], zs[iz - 1])
							if (!check(a)) continue
							val b = Point(xs[ix] - 1, ys[iy] - 1, zs[iz] - 1)
							yield(Cube(a, b))
						}
					}
				}
			}
		}
	}

	companion object {
		private val rangePattern = Regex("""(?<axis>[xyz])=(?<min>-?\d+)\.\.(?<max>-?\d+)""")
	}

	private fun parseCommand(line: String): Command {
		val parts = line.split(' ', limit = 2).map { it.trim() }.toList()
		val turnOn = when (parts[0]) {
			"on" -> true
			"off" -> false
			else -> fail("Invalid command in '$line'")
		}
		val cube = parseCube(parts[1])
		return Command(turnOn, cube)
	}

	private fun parseCube(line: String): Cube {
		// x=10..12,y=10..12,z=10..12
		val parts = line.split(",").map { it.trim() }.toList()
		check(parts.size == 3) { "Expected 3 ranges" }
		return Cube.create(
			parseRange("x", parts[0]),
			parseRange("y", parts[1]),
			parseRange("z", parts[2])
		)
	}

	private fun parseRange(axis: String, line: String): IntRange {
		// x=10..12
		val match = rangePattern.matchEntire(line).failIfNull()
		check(match.groups["axis"]!!.value == axis) { "Expected range for '$axis' in '$line'" }
		fun parse(group: String) = match.groups[group]!!.value.toInt()

		val min = parse("min")
		val max = parse("max")

		return min..max
	}

	@Test
	fun parse1() {
		check(parseRange("x", "x=-5..5") == -5..5)
		assertThrows<Exception> { parseRange("y", "x=-5..5") }
		assertThrows<Exception> { parseRange("x", "x=-5..5a") }
	}

	@Test
	fun test1_2() {
		val commands = loadLines("day22/_test.txt").map { parseCommand(it) }
		val history = History(commands)
		val universe = Cube.create(-50..50, -50..50, -50..50)
		val count = universe.points().count { history.check(it) }
		check(count == 590784)
	}

	@Test
	fun solve1() {
		val commands = loadLines("day22/_data.txt").map { parseCommand(it) }
		val history = History(commands)
		val universe = Cube.create(-50..50, -50..50, -50..50)
		val count = universe.points().count { history.check(it) }
		println("solve1: $count")
	}

	@Test
	fun test2_1() {
		val commands = loadLines("day22/_test.txt").map { parseCommand(it) }
		val universe = Cube.create(-50..50, -50..50, -50..50)
		val history = History(commands.filter { it.cube.inside(universe) })
		val volume = history.partition().sumOf { it.volume }
		check(volume == 590784L)
	}

	@Test
	fun test2_2() {
		val commands = loadLines("day22/_test2.txt").map { parseCommand(it) }
		val history = History(commands)
		val volume = history.partition().sumOf { it.volume }
		check(volume == 2758514936282235L)
	}

	/*
	// This takes 5min which isn't great but at least it is finite time
	@Test
	fun solve2_1() {
		val commands = loadLines("day22/_data.txt").map { parseCommand(it) }
		val volume = History(commands).partition().sumOf { it.volume }
		println("solve2_1: $volume")
	}
	*/

	@Test
	fun overlap_1() {
		check((5..7).overlap(1..9) == (5..7))
		check((5..7).overlap(1..6) == (5..6))
		check((5..7).overlap(0..5) == (5..5))
		check((5..7).overlap(0..4) == null)
		check((5..7).overlap(7..9) == (7..7))
		check((5..7).overlap(6..9) == (6..7))
	}

	class WeightedCube(val weight: Int, val cube: Cube) {
		val volume: Long get() = weight * cube.volume
		fun overlap(other: Cube): WeightedCube? =
			cube.overlap(other)?.let { WeightedCube(-weight, it) }
	}

	private fun solve2(commands: List<Command>): List<WeightedCube> {
		val stacked = mutableListOf<WeightedCube>()
		commands.forEach { command ->
			val added = command.cube
			val generated = stacked.mapNotNull { it.overlap(added) }
			stacked.addAll(generated)
			if (command.turnOn) stacked.add(WeightedCube(1, added))
		}
		return stacked
	}

	@Test
	fun test2_3() {
		val commands = loadLines("day22/_test2.txt").map { parseCommand(it) }
		val stacked = solve2(commands)
		check(stacked.sumOf { it.volume } == 2758514936282235L)
	}

	@Test
	fun solve2_2() {
		val commands = loadLines("day22/_data.txt").map { parseCommand(it) }
		val stacked = solve2(commands)
		val volume = stacked.sumOf { it.volume }
		println("solve2_2: $volume")
	}
}
