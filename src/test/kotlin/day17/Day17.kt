package day17

import failIfNull
import org.junit.jupiter.api.Test
import sgn0
import java.lang.Integer.*
import kotlin.math.sqrt

class Day17 {
    data class Target(val ax: Int, val ay: Int, val bx: Int, val by: Int) {
        companion object {
            // x=20..30, y=-10..-5
            fun create(x: IntRange, y: IntRange) = Target(
                min(x.first, x.last),
                min(y.first, y.last),
                max(x.first, x.last),
                max(y.first, y.last)
            )
        }

        fun rightDistance(position: Position): Boolean = position.x in ax..bx
        fun rightHeight(position: Position): Boolean = position.y in ay..by
        fun tooSlow(state: State): Boolean = state.position.x < ax && state.velocity.x <= 0
        fun isStable(state: State): Boolean = state.velocity.stable
        fun tooFar(state: State): Boolean = state.position.x > bx
        fun tooLow(state: State) = state.velocity.y < 0 && state.position.y < ay

        fun contains(position: Position): Boolean = rightDistance(position) && rightHeight(position)
        fun possible(state: State): Boolean = !tooFar(state) && !tooLow(state) && !tooSlow(state)
    }

    data class Velocity(val x: Int, val y: Int, val stable: Boolean = false) {
        fun step(): Velocity = Velocity(x - sgn0(x), y - 1, x == 0)
    }

    data class Position(val x: Int, val y: Int) {
        companion object {
            val zero = Position(0, 0)
        }

        fun move(v: Velocity) = Position(x + v.x, y + v.y)
    }

    data class State(val position: Position, val velocity: Velocity) {
        companion object {
            fun initial(x: Int, y: Int) = State(Position.zero, Velocity(x, y))
        }

        fun step(): State = State(position.move(velocity), velocity.step())
    }

    private fun simulate(state0: State): Sequence<State> =
        generateSequence(state0) { it.step() }

    @Test
    fun debug1() {
        val target = Target.create(x = 20..30, y = -10..-5)
        simulate(State.initial(7, 2)).takeWhile { target.possible(it) }.forEach { println(it) }
        println("----")
        simulate(State.initial(6, 3)).takeWhile { target.possible(it) }.forEach { println(it) }
        println("----")
        simulate(State.initial(9, 0)).takeWhile { target.possible(it) }.forEach { println(it) }
        println("----")
        simulate(State.initial(17, -4)).takeWhile { target.possible(it) }.forEach { println(it) }
        println("----")

        println("viable Y")
        viableY(target).toList().sortedBy { it.first }.forEach { println(it) }
        println("----")
    }

    @Test
    fun test1() {
        val target = Target.create(x = 20..30, y = -10..-5)
        check(solve1(target) == 45)
    }

    @Test
    fun task1() {
        // target area: x=128..160, y=-142..-88
        val target = Target.create(x = 128..160, y = -142..-88)
        solve1(target)
    }

    private fun solve1(target: Target): Int {
        val viableY = viableY(target)
        val optimal =
            crossRefStable(bestStableX(target), viableY) ?: crossRefViable(crossingX(target), viableY)
            ?: throw IllegalArgumentException("No solution found")

        simulate(State(Position.zero, optimal))
            .takeWhile { target.possible(it) }
            .forEach { println(it) }

        println("launch: $optimal")

        return (1..optimal.y).sum().apply { println("highest: $this") }
    }

    @Test
    fun test2() {
        val target = Target.create(x = 20..30, y = -10..-5)
        check(solve2(target) == 112)
    }

    @Test
    fun task2() {
        // target area: x=128..160, y=-142..-88
        val target = Target.create(x = 128..160, y = -142..-88)
        solve2(target)
    }

    private fun solve2(target: Target): Int {
        val viableY = viableY(target)
        val all = combineXY(viableY, allStableX(target), crossingX(target))

        all.forEach { println("${it.x},${it.y}") }

        return all.size.apply { println("count: $this") }
    }

    private fun combineXY(
        viableY: Map<Int, Set<Int>>,
        staleX: Map<Int, Set<Int>>,
        viableX: Map<Int, Set<Int>>
    ): Set<Velocity> {
        val maxSteps = viableY.maxOf { it.key }
        val staleXList = staleX
            .flatMap { (steps, vxs) -> vxs.map { vx -> Pair(steps, vx) } }
            .flatMap { (steps, vx) -> (steps..maxSteps).map { s -> Pair(s, vx) } }
        val viableXList = viableX
            .flatMap { (steps, vxs) -> vxs.map { vx -> Pair(steps, vx) } }

        val combinedX = staleXList.union(viableXList)
            .groupBy { (steps, _) -> steps }
            .map { (steps, vxs) -> Pair(steps, vxs.map { (_, vx) -> vx }.toSet()) }
            .toMap()

        val valid = viableY.keys.intersect(combinedX.keys).toList()

        return sequence {
            for (steps in valid) {
                val xs = combinedX[steps].orEmpty()
                val ys = viableY[steps].orEmpty()
                for (x in xs) {
                    for (y in ys) {
                        yield(Velocity(x, y))
                    }
                }
            }
        }.toSet()
    }

    private fun crossRefStable(x: Pair<Int, Int>?, y: Map<Int, Set<Int>>): Velocity? {
        if (x == null) return null
        val min = x.first
        return y
            .map { Pair(it.key, it.value.maxOrNull() ?: 0) }
            .sortedByDescending { it.first }
            .firstOrNull { it.first >= min }
            ?.let { Velocity(x.second, it.second) }
    }

    private fun crossRefViable(x: Map<Int, Set<Int>>, y: Map<Int, Set<Int>>): Velocity? {
        return x.keys.intersect(y.keys).maxOrNull()?.let {
            Velocity(x[it]?.maxOrNull() ?: 0, y[it]?.maxOrNull() ?: 0)
        }
    }

    private fun allStableX(target: Target): Map<Int, Set<Int>> {
        val result = mutableMapOf<Int, MutableSet<Int>>()
        val maxX = target.bx
        val vy = sqrt(Int.MAX_VALUE.toDouble()).toInt()
        for (vx in 0..maxX) {
            val state = State.initial(vx, vy)
            simulate(state)
                .withIndex()
                .takeWhile { !target.tooFar(it.value) && !target.tooSlow(it.value) }
                .filter { target.isStable(it.value) }
                .take(1)
                .filter { target.rightDistance(it.value.position) }
                .forEach { result.getOrPut(it.index) { mutableSetOf() }.add(vx) }
        }
        return result
    }

    private fun bestStableX(target: Target): Pair<Int, Int>? =
        allStableX(target)
            .maxByOrNull { it.key }
            ?.let { Pair(it.key, it.value.maxOrNull().failIfNull()) }

    private fun crossingX(target: Target): Map<Int, Set<Int>> {
        val result = mutableMapOf<Int, MutableSet<Int>>()
        val max = target.bx
        val vy = sqrt(Int.MAX_VALUE.toDouble()).toInt()
        for (vx in 0..max) {
            val state = State.initial(vx, vy)
            simulate(state)
                .withIndex()
                .takeWhile { !target.tooFar(it.value) && !target.tooSlow(it.value) && !target.isStable(it.value) }
                .filter { target.rightDistance(it.value.position) }
                .forEach { result.getOrPut(it.index) { mutableSetOf() }.add(vx) }
        }
        return result
    }

    private fun viableY(target: Target): Map<Int, Set<Int>> {
        val result = mutableMapOf<Int, MutableSet<Int>>()
        check(target.ay < 0 && target.by < 0) { "It only works down " }

        val max = -target.ay
        for (vy in 0..max) {
            val upfront = vy * 2 + 1
            val state = State.initial(0, -vy - 1)
            simulate(state)
                .withIndex()
                .takeWhile { !target.tooLow(it.value) }
                .filter { target.rightHeight(it.value.position) }
                .forEach {
                    result.getOrPut(it.index) { mutableSetOf() }.add(-vy - 1)
                    result.getOrPut(upfront + it.index) { mutableSetOf() }.add(vy)
                }
        }
        return result
    }
}