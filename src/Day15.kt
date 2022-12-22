import kotlin.math.abs

private val LINE_REGEX = "Sensor\\s+at\\s+x=(?<sensorX>-?\\d+),\\s+y=(?<sensorY>-?\\d+):\\s+closest\\s+beacon\\s+is\\s+at\\s+x=(?<beaconX>-?\\d+),\\s+y=(?<beaconY>-?\\d+)".toRegex()
private const val TUNING_FACTOR_X = 4000000L

data class Sensor(
    val position: Pair<Int, Int>,
    val closestBeacon: Pair<Int, Int>
)

data class Range(
    val start: Int,
    val end: Int
)

fun main() {

    fun parseSensors(input: List<String>) = input.map {
        val matchResult = LINE_REGEX.matchEntire(it)!!
        Sensor(
            matchResult.groups["sensorX"]!!.value.toInt() to matchResult.groups["sensorY"]!!.value.toInt(),
            matchResult.groups["beaconX"]!!.value.toInt() to matchResult.groups["beaconY"]!!.value.toInt()
        )
    }

    fun combineRanges(ranges: Set<Range>): Set<Range> {
        if (ranges.isEmpty() || ranges.size == 1) {
            return ranges
        }
        val sortedRanges = ranges.sortedBy { it.start }

        var currentStart = sortedRanges[0].start
        var currentEnd = sortedRanges[0].end

        val result = mutableSetOf<Range>()

        for (i in 1 until sortedRanges.size) {
            val range = sortedRanges[i]
            if (range.start <= currentEnd + 1) {
                if (range.end > currentEnd) {
                    currentEnd = range.end
                }
            } else {
                result.add(Range(currentStart, currentEnd))
                currentStart = range.start
                currentEnd = range.end
            }
        }
        result.add(Range(currentStart, currentEnd))
        return result
    }

    fun manhattanDistance(first: Pair<Int, Int>, second: Pair<Int, Int>): Int =
        abs(first.first - second.first) + abs(first.second - second.second)

    fun getCoverageRanges(y: Int, sensors: List<Sensor>, maxPos: Int? = null): Set<Range> {
        val ranges = mutableSetOf<Range>()
        for (sensor in sensors) {
            val distance = manhattanDistance(sensor.position, sensor.closestBeacon)
            val leftPoint = sensor.position.first - distance to sensor.position.second
            val rightPoint = sensor.position.first + distance to sensor.position.second
            val topPoint = sensor.position.first to sensor.position.second - distance
            val bottomPoint = sensor.position.first to sensor.position.second + distance

            if ((y >= topPoint.second && y <= sensor.position.second) ||
                (y >= sensor.position.second && y <= bottomPoint.second)) {
                val difference = abs(sensor.position.second - y)
                maxPos?.let {
                    ranges.add(Range(maxOf(leftPoint.first + difference, 0), minOf(rightPoint.first - difference, it)))
                } ?: ranges.add(Range(leftPoint.first + difference, rightPoint.first - difference))
            }
        }
        return combineRanges(ranges)
    }

    fun part1(input: List<String>, y: Int): Int {
        val sensors = parseSensors(input)
        val combinedRanges = getCoverageRanges(y, sensors)

        val sensorsAndBeaconsInY = sensors.flatMap { listOf(it.position, it.closestBeacon) }
                            .filter { it.second == y }

        val sumRangesSize = combinedRanges.sumOf { it.end - it.start + 1 }
        val numSensorsAndBeaconsInRanges = combinedRanges.count { range ->
            sensorsAndBeaconsInY.any { point -> point.first >= range.start && point.first <= range.end }
        }

        return sumRangesSize - numSensorsAndBeaconsInRanges
    }

    fun part2(input: List<String>, maxPos: Int): Long {
        val sensors = parseSensors(input)

        for (y in 0..maxPos) {
            val combinedRanges = getCoverageRanges(y, sensors, maxPos)
            if (combinedRanges.size > 1) {
                val rangeX = combinedRanges.windowed(2).first { it[1].start > it[0].end + 1 }[0].end + 1
                return rangeX * TUNING_FACTOR_X + y
            }
        }
        return -1L
    }

    val testInput = readInput("Day15_test")
    check(part1(testInput, 10) == 26)
    check(part2(testInput, 20) == 56000011L)

    val input = readInput("Day15")
    println(part1(input, 2000000)) // 5838453
    println(part2(input, 4000000)) // 12413999391794
}