private const val FIRST_OPPONENT_CODE = 'A'
private const val FIRST_SELF_CODE = 'X'

private const val LOSE_SCORE = 0
private const val DRAW_SCORE = 3
private const val WIN_SCORE = 6

enum class Shape(private val score: Int) {
    ROCK(1) {
        override val beats: Shape
            get() = SCISSORS
    },
    PAPER(2) {
        override val beats: Shape
            get() = ROCK
    },
    SCISSORS(3) {
        override val beats: Shape
            get() = PAPER
    };

    fun playAgainst(other: Shape) = score + when(other) {
        this -> DRAW_SCORE
        beats -> WIN_SCORE
        else -> LOSE_SCORE
    }

    abstract val beats: Shape
}

enum class RoundResult {
    LOSE {
        override fun calculateSelfShape(opponentShape: Shape) = opponentShape.beats
    }, DRAW {
        override fun calculateSelfShape(opponentShape: Shape) = opponentShape
    }, WIN {
        override fun calculateSelfShape(opponentShape: Shape) = Shape.values().first { it.beats == opponentShape }
    };

    abstract fun calculateSelfShape(opponentShape: Shape): Shape
}

fun main() {
    fun play(input: List<String>, isPart1: Boolean) = input.sumOf {
        val (opponentShapeCode, selfCode) = it.split(" ")
        val opponentShape = Shape.values()[opponentShapeCode.first() - FIRST_OPPONENT_CODE]
        val selfShape = if(isPart1) {
            Shape.values()[selfCode.first() - FIRST_SELF_CODE]
        } else {
            RoundResult.values()[selfCode.first() - FIRST_SELF_CODE].calculateSelfShape(opponentShape)
        }
        selfShape.playAgainst(opponentShape)
    }

    fun part1(input: List<String>) = play(input = input, isPart1 = true)

    fun part2(input: List<String>) = play(input = input, isPart1 = false)

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println(part1(input)) // 15632
    println(part2(input)) // 14416
}
