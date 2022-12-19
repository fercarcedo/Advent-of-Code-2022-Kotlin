private val LINE_REGEX = "(?<x>\\d+),(?<y>\\d+)(?:\\s+->\\s+)?".toRegex()
private val SAND_START = 500 to 0

data class Line(
    val start: Pair<Int, Int>,
    val end: Pair<Int, Int>
)

fun Line.isVertical() = start.first == end.first

enum class Item(private val text: String) {
    AIR("."), ROCK("#"), SAND("o");

    override fun toString(): String {
        return text
    }
}

fun main() {

    fun parseLines(input: List<String>) = input.flatMap { line ->
        val matches = LINE_REGEX.findAll(line).toList()
        (1 until matches.count()).map {
            Line(
                matches[it - 1].groups["x"]!!.value.toInt() to matches[it - 1].groups["y"]!!.value.toInt(),
                matches[it].groups["x"]!!.value.toInt() to matches[it].groups["y"]!!.value.toInt()
            )
        }
    }

    fun sandFallingIntoAbyss(
        sandX: Int,
        sandY: Int,
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
        partOne: Boolean = true
    ) = if (partOne) {
        sandY - minY < 0 ||
        sandY > maxY ||
        sandX - minX < 0 ||
        sandX > maxX
    } else {
        sandY - minY < 0 ||
        sandY > maxY + 2
    }

    fun checkIfAirInPosition(x: Int, y: Int, minX: Int, minY: Int, maxX: Int, maxY: Int, minimumX: Int, gridMap: Map<Pair<Int, Int>, Item>, partOne: Boolean): Boolean {
        if (sandFallingIntoAbyss(x, y, minX, minY, maxX, maxY, partOne)) {
            return true
        }
        if (!partOne && y == maxY + 2) {
            return false
        }
        return x to y !in gridMap ||
                gridMap[x to y] == Item.AIR
    }

    fun fillGrid(gridMap: MutableMap<Pair<Int, Int>, Item>, lines: List<Line>) {
        for (line in lines) {
            if (line.isVertical()) {
                val range = if (line.start.second < line.end.second) {
                    (line.start.second until line.end.second + 1)
                } else {
                    (line.start.second downTo line.end.second)
                }
                for (y in range) {
                    gridMap[line.start.first to y] = Item.ROCK
                }
            } else {
                val range = if (line.start.first < line.end.first) {
                    (line.start.first until line.end.first + 1)
                } else {
                    (line.start.first downTo line.end.first)
                }
                for (x in range) {
                    gridMap[x to line.end.second] = Item.ROCK
                }
            }
        }
    }

    fun play(input: List<String>, partOne: Boolean): Int {
        val lines = parseLines(input)
        val minX = minOf(lines.minOf { minOf(it.start.first, it.end.first) }, SAND_START.first)
        val minY = minOf(lines.minOf { minOf(it.start.second, it.end.second) }, SAND_START.second)
        val minimumX = minOf(lines.minOf { minOf(it.start.first, it.end.first) }, SAND_START.first)
        val maxX = maxOf(lines.maxOf { maxOf(it.start.first, it.end.first) }, SAND_START.first)
        val maxY = maxOf(lines.maxOf { maxOf(it.start.second, it.end.second) }, SAND_START.second)

        val gridMap = mutableMapOf<Pair<Int, Int>, Item>()
        fillGrid(gridMap, lines)

        var lastSandX = SAND_START.first
        var lastSandY = SAND_START.second

        while (!sandFallingIntoAbyss(lastSandX, lastSandY, minX, minY, maxX, maxY, partOne) &&
            gridMap[SAND_START.first to SAND_START.second] != Item.SAND) {
            var sandX = SAND_START.first
            var sandY = SAND_START.second

            gridMap[sandX to sandY] = Item.SAND

            var blocked = false

            while (!blocked && !sandFallingIntoAbyss(sandX, sandY, minX, minY, maxX, maxY, partOne)) {
                if (checkIfAirInPosition(sandX, sandY + 1, minX, minY, maxX, maxY, minimumX, gridMap, partOne)) {
                    gridMap[sandX to sandY] = Item.AIR
                    sandY += 1
                } else if (checkIfAirInPosition(sandX - 1, sandY + 1, minX, minY, maxX, maxY, minimumX, gridMap, partOne)) {
                    gridMap[sandX to sandY] = Item.AIR
                    sandY += 1
                    sandX -= 1
                } else if (checkIfAirInPosition(sandX + 1 , sandY + 1, minX, minY, maxX, maxY, minimumX, gridMap, partOne)) {
                    gridMap[sandX to sandY] = Item.AIR
                    sandY += 1
                    sandX += 1
                } else {
                    blocked = true
                }
                if (!sandFallingIntoAbyss(sandX, sandY, minX, minY, maxX, maxY, partOne)) {
                    gridMap[sandX to sandY] = Item.SAND
                }
                lastSandX = sandX
                lastSandY = sandY
            }
        }

        return gridMap.count { it.value == Item.SAND }
    }

    fun part1(input: List<String>) = play(input, true)

    fun part2(input: List<String>) = play(input, false)

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println(part1(input)) // 665
    println(part2(input)) // 25434
}