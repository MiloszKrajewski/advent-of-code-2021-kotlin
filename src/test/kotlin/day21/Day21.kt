package day21

import failIfNull
import org.junit.jupiter.api.Test

class Day21 {
	interface Dice {
		fun next(): Int
	}

	class DeterministicDice : Dice {
		private var _current: Int = 0 // 0..99

		override fun next(): Int {
			val result = _current + 1
			_current = (_current + 1) % 100
			return result
		}
	}

	class Player(val name: String, initial: Int, val score: Int = 0) {
		private val _position: Int = initial

		fun advance(steps: Int): Player {
			val p = (_position - 1 + steps) % 10 + 1
			return Player(name, p, score + p)
		}
	}

	data class SimpleGame(val a: Player, val b: Player, val turn: Int = 0) {
		fun hasWinner(threshold: Int) = a.score >= threshold || b.score >= threshold
		fun loser(): Player = if (a.score > b.score) b else a
		fun previous(): Player = if (turn % 2 == 0) b else a
		fun move(dice: Dice): SimpleGame {
			val steps = dice.next() + dice.next() + dice.next()
			val turn0 = turn % 2 == 0
			return SimpleGame(
				if (turn0) a.advance(steps) else a,
				if (turn0) b else b.advance(steps),
				turn + 1
			)
		}
	}

	@Test
	fun test1_1() {
		val d = DeterministicDice()
		val a = Player("a", 4)
		val b = Player("b", 8)
		val g = SimpleGame(a, b)
		var x: SimpleGame = g

		fun validate(expected: Int) {
			x = x.move(d)
			check(x.previous().score == expected)
		}

		validate(10)
		validate(3)
		validate(14)
		validate(9)
		validate(20)
		validate(16)
		validate(26)
		validate(22)
	}

	private fun play1(dice: Dice, a: Player, b: Player): Int {
		var g = SimpleGame(a, b)

		while (true) {
			g = g.move(dice)
			if (g.hasWinner(1000)) {
				val score = g.loser().score
				val throws = g.turn * 3
				val result = score * throws
				println("$score x $throws = $result")
				return result
			}
		}
	}

	@Test
	fun test1_2() {
		val d = DeterministicDice()
		val a = Player("a", 4)
		val b = Player("b", 8)
		val r = play1(d, a, b)
		check(r == 739785)
	}

	@Test
	fun solve1() {
		val d = DeterministicDice()
		val a = Player("a", 4)
		val b = Player("b", 7)
		play1(d, a, b)
	}

	data class DiracGame(val a: Player, val b: Player, val weight: Long = 1, val turn: Int = 0) {
		companion object {
			val dice: List<Pair<Int, Long>> =
				listOf(3 to 1, 4 to 3, 5 to 6, 6 to 7, 7 to 6, 8 to 3, 9 to 1)
		}

		fun winner(threshold: Int): Player? =
			if (a.score >= threshold) a else if (b.score >= threshold) b else null

		fun move(): List<DiracGame> {
			val turn0 = turn % 2 == 0
			return dice.map { (steps, multiplier) ->
				DiracGame(
					if (turn0) a.advance(steps) else a,
					if (turn0) b else b.advance(steps),
					weight * multiplier,
					turn + 1
				)
			}
		}
	}

	@Test
	fun test2_1() {
		val a = Player("a", 4)
		val b = Player("b", 8)
		val w = play2(a, b)
		check(w["a"] == 444356092776315L)
		check(w["b"] == 341960390180808L)
	}

	@Test
	fun solve2() {
		val a = Player("a", 4)
		val b = Player("b", 7)
		val w = play2(a, b)
		val max = w.values.maxOrNull().failIfNull()
		println("solve2: $w, max=$max")
	}

	private fun play2(a: Player, b: Player): Map<String, Long> {
		check(a.name != b.name) { "Players have same name" }
		val game = DiracGame(a, b)
		val wins = mutableMapOf<String, Long>()
		play2(game, wins)
		return wins
	}

	private fun play2(game: DiracGame, wins: MutableMap<String, Long>, level: Int = 0) {
		val winner = game.winner(21)
		if (winner != null) {
			wins.compute(winner.name) { _, u -> (u ?: 0) + game.weight }
		} else {
			game.move().forEach { play2(it, wins, level + 1) }
		}
	}
}
