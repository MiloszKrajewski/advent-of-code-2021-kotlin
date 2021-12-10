package day04

import loadLines
import round
import kotlin.test.Test

class Day04 {
    @Test
    fun part1() {
        val test = loadLines("day04/_test.txt")
        println(findBingo(test))
        println(findLastWinningBoard(test))

        val data = loadLines("day04/_data.txt")
        println(findBingo(data))
        println(findLastWinningBoard(data))
    }

    private fun findBingo(lines: List<String>): Int {
        val numbers = lines[0].split(',').map { it.trim().toInt() }
        val boards = loadBoards(lines.drop(2))

        for (number in numbers)
            for (board in boards)
                if (board.mark(number))
                    return number * board.sumUnmarked()

        return -1
    }

    private fun findLastWinningBoard(lines: List<String>): Int {
        val numbers = lines[0].split(',').map { it.trim().toInt() }
        val boards = loadBoards(lines.drop(2))
        val removed = mutableSetOf<Board>()

        for (number in numbers) {
            for (board in boards) {
                if (removed.contains(board))
                    continue
                if (board.mark(number)) {
                    removed.add(board)
                    if (removed.size == boards.size)
                        return number * board.sumUnmarked()
                }
            }
        }

        return -1
    }


    class Board(
        private val values: IntArray
    ) {
        private var ticks: BooleanArray = BooleanArray(values.size)

        fun mark(number: Int): Boolean {
            val index = values.indexOf(number)
            if (index < 0) return false
            ticks[index] = true
            return checkHorizontal(index) || checkVertical(index)
        }

        private fun checkVertical(index: Int): Boolean {
            var colStart = index
            while (colStart - 5 >= 0) colStart -= 5
            return (0 until 5).all { ticks[colStart + it * 5] }
        }

        private fun checkHorizontal(index: Int): Boolean {
            val rowStart = index.round(5)
            return (0 until 5).all { ticks[rowStart + it] }
        }

        fun sumUnmarked(): Int =
            ticks
                .withIndex()
                .filter { (_, b) -> !b }
                .sumOf { (i, _) -> values[i] }
    }

    private fun loadBoards(lines: List<String>): List<Board> {
        fun appendBlock(result: MutableList<Board>, block: List<String>) {
            if (block.isEmpty()) return
            val board = Board(
                block
                    .flatMap { it.split(' ') }
                    .filter { it.isNotBlank() }
                    .map { it.toInt() }
                    .toIntArray()
            )
            result.add(board)
        }

        val result = mutableListOf<Board>()
        val block = mutableListOf<String>()
        for (line in lines) {
            if (line == "") {
                appendBlock(result, block)
                block.clear()
            } else {
                block.add(line)
            }
        }
        appendBlock(result, block)
        return result.toList()
    }
}
