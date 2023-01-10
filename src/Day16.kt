private val LINE_REGEX = "Valve\\s+(?<valveName>[A-Z]{2})\\s+has\\s+flow\\s+rate=(?<flowRate>\\d+);\\s+tunnels?\\s+leads?\\s+to\\s+valves?\\s+(?<valves>[A-Z]{2}(,\\s+[A-Z]{2})*)".toRegex()
private const val TIME_TO_ESCAPE_IN_MINUTES_PART_ONE = 30
private const val TIME_TO_ESCAPE_IN_MINUTES_PART_TWO = 26
private const val ORIGIN_VALVE_NAME = "AA"

data class Valve(
    val name: String,
    val flowRate: Int,
    val tunnels: List<String> = listOf()
)

fun Set<Long>.disjointPaths() = sequence {
    this@disjointPaths.forEachIndexed { firstBitmapIndex, firstBitmap ->
        this@disjointPaths.forEachIndexed { secondBitmapIndex, secondBitmap ->
            if (firstBitmapIndex != secondBitmapIndex &&
                firstBitmap and secondBitmap == 0L) {
                yield(firstBitmap to secondBitmap)
            }
        }
    }
}

fun main() {

    fun floyd(valvesMap: Map<String, Valve>): Array<IntArray> {
        val distances = Array(valvesMap.size) { IntArray(valvesMap.size) { Int.MAX_VALUE } }
        for ((valveIndex, valve) in valvesMap.values.withIndex()) {
            for (destinationValve in valve.tunnels) {
                distances[valveIndex][valvesMap.values.indexOfFirst { it.name == destinationValve }] = 1
            }
        }
        for (valveIndex in valvesMap.values.indices) {
            distances[valveIndex][valveIndex] = 0
        }
        for (k in valvesMap.values.indices) {
            for (i in valvesMap.values.indices) {
                for (j in valvesMap.values.indices) {
                    if (distances[i][k] < Int.MAX_VALUE &&
                        distances[k][j] < Int.MAX_VALUE &&
                        distances[i][j] > distances[i][k] + distances[k][j]) {
                        distances[i][j] = distances[i][k] + distances[k][j]
                    }
                }
            }
        }
        return distances
    }

    fun calculateMaxPressures(valves: List<Valve>,
                              distances: Array<IntArray>,
                              valvesMap: Map<String, Valve>,
                              bitmapValves: Map<Valve, Long>,
                              previousValve: Valve,
                              time: Int,
                              resultsMap: MutableMap<Long, Int> = mutableMapOf(),
                              points: Int = 0,
                              visitedValves: MutableSet<Valve> = mutableSetOf()): Map<Long, Int> {
        var currentPoints = points
        var timeLeft = time
        for (valve in valves) {
            if (valve !in visitedValves) {
                val valveIndex = valvesMap.values.indexOfFirst { currentValve -> currentValve.name == valve.name }
                val previousValveIndex = valvesMap.values.indexOfFirst { currentValve -> currentValve.name == previousValve.name }
                val timeRequired = 1 + distances[previousValveIndex][valveIndex]

                if (timeRequired <= timeLeft) {
                    timeLeft -= timeRequired
                    currentPoints += valve.flowRate * timeLeft
                    visitedValves.add(valve)
                    val valvesBitmap = visitedValves.fold(0L) { acc, value -> acc or bitmapValves[value]!! }
                    val lastBitmapValue = resultsMap.getOrDefault(valvesBitmap, Int.MIN_VALUE)
                    resultsMap[valvesBitmap] = maxOf(lastBitmapValue, currentPoints)

                    calculateMaxPressures(valves, distances, valvesMap, bitmapValves, valve, timeLeft, resultsMap, currentPoints, visitedValves)

                    visitedValves.remove(valve)
                    currentPoints -= valve.flowRate * timeLeft
                    timeLeft += timeRequired
                }
            }
        }
        return resultsMap
    }

    fun play(input: List<String>, timeToEscape: Int): Map<Long, Int> {
        val valvesMap = input.map { line ->
            val matchResult = LINE_REGEX.matchEntire(line)
            val valveName = matchResult!!.groups["valveName"]!!.value
            val flowRate = matchResult.groups["flowRate"]!!.value.toInt()
            val valves = matchResult.groups["valves"]!!.value.split(",").map { it.trim() }
            Valve(valveName, flowRate, valves)
        }.associateBy { it.name }
        val originValve = valvesMap[ORIGIN_VALVE_NAME]!!

        val distances = floyd(valvesMap)
        val filteredValves = valvesMap.values.filter { it.flowRate > 0 }
        val bitmapValves = filteredValves.mapIndexed { i, valve -> valve to (1L shl i) }.toMap()

        return calculateMaxPressures(filteredValves, distances, valvesMap, bitmapValves, originValve, timeToEscape)
    }

    fun part1(input: List<String>) = play(input, TIME_TO_ESCAPE_IN_MINUTES_PART_ONE).values.max()

    fun part2(input: List<String>): Int {
        val resultsMap = play(input, TIME_TO_ESCAPE_IN_MINUTES_PART_TWO)

        return resultsMap.keys.disjointPaths()
            .maxOf { resultsMap[it.first]!! + resultsMap[it.second]!! }
    }

    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    println(part1(input)) // 1845
    println(part2(input)) // 2286
}