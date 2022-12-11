import kotlin.math.abs

enum class MotionDirection {
    UP, DOWN, RIGHT, LEFT
}

data class Motion(val direction: MotionDirection, val numberOfSteps: Int)

private val LINE_REGEX = "(?<direction>[UDRL])\\s+(?<steps>\\d+)".toRegex()

private const val NUM_TAILS = 9

fun main() {

    fun isAdjacent(headPosition: Pair<Int, Int>, tailPosition: Pair<Int, Int>) =
        abs(headPosition.first - tailPosition.first) <= 1 && abs(headPosition.second - tailPosition.second) <= 1

    fun parseMotions(input: List<String>) = input.map {
        val match = LINE_REGEX.matchEntire(it)
        val direction = when(match!!.groups["direction"]!!.value) {
            "U" -> MotionDirection.UP
            "D" -> MotionDirection.DOWN
            "R" -> MotionDirection.RIGHT
            else -> MotionDirection.LEFT
        }
        Motion(direction, match.groups["steps"]!!.value.toInt())
    }

    fun moveHead(headPosition: Pair<Int, Int>, motion: Motion) = when (motion.direction) {
        MotionDirection.UP -> {
            headPosition.copy(second = headPosition.second + 1)
        }
        MotionDirection.DOWN -> {
            headPosition.copy(second = headPosition.second - 1)
        }
        MotionDirection.RIGHT -> {
            headPosition.copy(first = headPosition.first + 1)
        }
        else -> {
            headPosition.copy(first = headPosition.first - 1)
        }
    }

    fun moveKnot(headPosition: Pair<Int, Int>, tails: List<Pair<Int, Int>>, tailIndex: Int): Pair<Int, Int> {
        val previousKnotPos = if (tailIndex == 0) headPosition else tails[tailIndex - 1]
        var tailPosition = tails[tailIndex]

        if (!isAdjacent(previousKnotPos, tailPosition)) {
            if (previousKnotPos.second == tailPosition.second && abs(previousKnotPos.first - tailPosition.first) == 2) {
                tailPosition = tailPosition.copy(first = if (previousKnotPos.first > tailPosition.first) tailPosition.first + 1 else tailPosition.first - 1)
            } else if (previousKnotPos.first == tailPosition.first && abs(previousKnotPos.second - tailPosition.second) == 2) {
                tailPosition = tailPosition.copy(second = if (previousKnotPos.second > tailPosition.second) tailPosition.second + 1 else tailPosition.second - 1)
            } else if (abs(previousKnotPos.first - tailPosition.first) >= 1 && abs(previousKnotPos.second - tailPosition.second) >= 1) {
                tailPosition = tailPosition.copy(
                    first = if (previousKnotPos.first > tailPosition.first) tailPosition.first + 1 else tailPosition.first - 1,
                    second = if (previousKnotPos.second > tailPosition.second) tailPosition.second + 1 else tailPosition.second - 1
                )
            }
        }
        return tailPosition
    }

    fun executeMotions(motions: List<Motion>): List<Set<Pair<Int, Int>>> {
        var headPosition: Pair<Int, Int> = 0 to 0
        val tails: MutableList<Pair<Int, Int>> = mutableListOf()
        val tailsPositions: MutableList<MutableSet<Pair<Int, Int>>> = mutableListOf()
        repeat(NUM_TAILS) {
            tails.add(0 to 0)
            tailsPositions.add(mutableSetOf())
        }

        for (motion in motions) {
            for (i in 0 until motion.numberOfSteps) {
                headPosition = moveHead(headPosition, motion)

                for (i in 0 until tails.size) {
                    tails[i] = moveKnot(headPosition, tails, i)
                    tailsPositions[i].add(tails[i])
                }
            }
        }
        return tailsPositions
    }

    fun play(input: List<String>): List<Set<Pair<Int, Int>>> {
        val motions = parseMotions(input)
        return executeMotions(motions)
    }

    fun part1(input: List<String>) = play(input).first().size

    fun part2(input: List<String>) = play(input).last().size

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 1)

    val testInput2 = readInput("Day09_test2")
    check(part2(testInput2) == 36)

    val input = readInput("Day09")
    println(part1(input)) // 6271
    println(part2(input)) // 2458
}
