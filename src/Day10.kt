private const val NOOP_INSTRUCTION = "noop"
private val ADDX_INSTRUCTION_REGEX = "addx\\s+(?<value>-?\\d+)".toRegex()

private const val CRT_WIDTH = 40
private const val CRT_HEIGHT = 6

sealed class Instruction {
    object Noop: Instruction() {
        override val cycles: Int
            get() = 1

        override fun toString() = "Noop"

        override fun execute(registerX: Int) = registerX
    }

    data class Addx(val value: Int): Instruction() {
        override val cycles: Int
            get() = 2

        override fun execute(registerX: Int) = registerX + value
    }

    abstract val cycles: Int
    abstract fun execute(registerX: Int): Int
}

data class ExecutionResult(
    val signalStrengths: List<Int>,
    val crt: List<List<Char>>
)

class Computer(private val crtEnabled: Boolean) {
    private var registerX = 1
    private var cycle = 1
    private var signalStrengths = mutableListOf<Int>()
    private var crt = mutableListOf(mutableListOf<Char>())

    fun execute(instructions: List<Instruction>): ExecutionResult {
        checkCycle()
        for (instruction in instructions) {
            for (i in 0 until instruction.cycles) {
                cycle++
                if (i < instruction.cycles - 1) {
                    checkCycle()
                }
            }
            registerX = instruction.execute(registerX)
            checkCycle()
        }
        return ExecutionResult(signalStrengths, crt)
    }

    private fun checkCycle() {
        if (crtEnabled) {
            handleCrt()
        }
        if (cycle in listOf(20, 60, 100, 140, 180, 220)) {
            signalStrengths.add(cycle * registerX)
        }
    }

    private fun handleCrt() {
        if (cycle <= CRT_WIDTH * CRT_HEIGHT) {
            val crtPosition = (cycle - 1) % 40
            crt.last().add(if (crtPosition >= (registerX - 1) && crtPosition <= (registerX + 1)) '#' else '.')
            if (cycle % 40 == 0 && cycle < CRT_WIDTH * CRT_HEIGHT) {
                crt.add(mutableListOf())
            }
        }
    }
}

fun List<List<Char>>.crtToString() = joinToString("\n") { it.joinToString("") }
fun main() {

    fun play(input: List<String>, crtEnabled: Boolean): ExecutionResult {
        val instructions = input.map {
            if (it.startsWith(NOOP_INSTRUCTION)) {
                Instruction.Noop
            } else {
                Instruction.Addx(ADDX_INSTRUCTION_REGEX.matchEntire(it)!!.groups["value"]!!.value.toInt())
            }
        }
        return Computer(crtEnabled).execute(instructions)
    }

    fun part1(input: List<String>) = play(input, false).signalStrengths.sum()

    fun part2(input: List<String>) = play(input, true).crt

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)
    check(part2(testInput).crtToString() == """
        ##..##..##..##..##..##..##..##..##..##..
        ###...###...###...###...###...###...###.
        ####....####....####....####....####....
        #####.....#####.....#####.....#####.....
        ######......######......######......####
        #######.......#######.......#######.....
    """.trimIndent()
    )

    val input = readInput("Day10")
    println(part1(input)) // 14360
    println(part2(input).crtToString()) // ###...##..#..#..##..####.###..####.####.
                                        // #..#.#..#.#.#..#..#.#....#..#.#.......#.
                                        // ###..#....##...#..#.###..#..#.###....#..
                                        // #..#.#.##.#.#..####.#....###..#.....#...
                                        // #..#.#..#.#.#..#..#.#....#.#..#....#....
                                        //###...###.#..#.#..#.####.#..#.####.####.
}
