private const val START_OF_PACKET_MARKER_LENGTH = 4
private const val START_OF_MESSAGE_MARKER_LENGTH = 14
private const val MARKER_NOT_FOUND = -1

fun main() {

    fun play(input: List<String>, markerLength: Int): Int {
        val line = input[0]
        for (i in 0 until line.length - markerLength) {
            val possibleMarker = line.substring(i, i + markerLength)
            if (possibleMarker.toSet().size == possibleMarker.length) {
                return i + markerLength
            }
        }
        return MARKER_NOT_FOUND
    }

    fun part1(input: List<String>) = play(input, START_OF_PACKET_MARKER_LENGTH)

    fun part2(input: List<String>) = play(input, START_OF_MESSAGE_MARKER_LENGTH)

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 19)

    val testInput2 = readInput("Day06_test2")
    check(part1(testInput2) == 5)
    check(part2(testInput2) == 23)

    val testInput3 = readInput("Day06_test3")
    check(part1(testInput3) == 6)
    check(part2(testInput3) == 23)

    val testInput4 = readInput("Day06_test4")
    check(part1(testInput4) == 10)
    check(part2(testInput4) == 29)

    val testInput5 = readInput("Day06_test5")
    check(part1(testInput5) == 11)
    check(part2(testInput5) == 26)

    val input = readInput("Day06")
    println(part1(input)) // 1804
    println(part2(input)) // 2508
}
