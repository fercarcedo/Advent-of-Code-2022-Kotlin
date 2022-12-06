data class Move(
    val numCrates: Int,
    val fromStack: Int,
    val toStack: Int
)

fun main() {
    fun parseStacks(input: List<String>): List<ArrayDeque<Char>> {
        val stackNumbersLineRegex = "(\\d|\\s)+".toRegex()
        val stackNumbersLineIndex = input.indexOfFirst { stackNumbersLineRegex.matches(it) }
        val lineIndexToStackNumberMap = "\\d+".toRegex().findAll(input[stackNumbersLineIndex]).flatMap {
            it.groups
        }.associate { it!!.range.first to it.value.toInt() }
        val numStacks = lineIndexToStackNumberMap.maxBy { it.value }.value

        val stacks = mutableListOf<ArrayDeque<Char>>()
        repeat(numStacks) { stacks.add(ArrayDeque()) }

        val regex = "\\[(?<crate>[A-Z])\\]".toRegex()
        for (i in stackNumbersLineIndex - 1 downTo 0) {
            val line = input[i]
            regex.findAll(line).forEach {
                val crateGroup = it.groups["crate"]
                val crateIndex = crateGroup!!.range.first
                val crate = crateGroup.value
                val stackNumber = lineIndexToStackNumberMap[crateIndex]!!
                stacks[stackNumber - 1].addFirst(crate.first())
            }
        }
        return stacks
    }

    fun parseMovements(input: List<String>): List<Move> {
        val moves = mutableListOf<Move>()
        val regexMove = "move (?<numCrates>\\d+) from (?<fromStack>\\d+) to (?<toStack>\\d+)".toRegex()
        val firstMoveLine = input.indexOfFirst { regexMove.matches(it) }
        for (i in firstMoveLine until input.size) {
            val matchGroups = regexMove.matchEntire(input[i])!!.groups
            moves.add(Move(matchGroups["numCrates"]!!.value.toInt(), matchGroups["fromStack"]!!.value.toInt() - 1, matchGroups["toStack"]!!.value.toInt() - 1))
        }
        return moves
    }

    fun executeMovements(stacks: List<ArrayDeque<Char>>,
                         movements: List<Move>,
                         allowMovingMultipleCratesAtOnce: Boolean) {
        movements.forEach { move ->
            val crates = mutableListOf<Char>()
            repeat(move.numCrates) {
                crates.add(stacks[move.fromStack].removeFirst())
            }
            if (move.numCrates > 1 && allowMovingMultipleCratesAtOnce) {
                crates.reverse()
            }
            crates.forEach { stacks[move.toStack].addFirst(it) }
        }
    }

    fun play(input: List<String>,
             allowMovingMultipleCratesAtOnce: Boolean = false): String {
        val stacks = parseStacks(input)
        val movements = parseMovements(input)
        executeMovements(stacks, movements, allowMovingMultipleCratesAtOnce)

        return stacks.map { it.first() }.joinToString("")
    }

    fun part1(input: List<String>) = play(input)

    fun part2(input: List<String>) = play(input, true)

    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println(part1(input)) // ZRLJGSCTR
    println(part2(input)) // PRTTGRFPB
}
