private const val CHAMBER_WIDTH = 7
private const val MAX_ROCKS_PART_ONE = 2022L
private const val MAX_ROCKS_PART_TWO = 1000000000000L

enum class RockType {
    HORIZONTAL_LINE {
        override fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>> {
            return (bottomLeftPosition.first until bottomLeftPosition.first + 4).map {
                it to bottomLeftPosition.second
            }
        }
    }, PLUS {
        override fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>> {
            return listOf(
                bottomLeftPosition.first + 1 to bottomLeftPosition.second,
                bottomLeftPosition.first to bottomLeftPosition.second + 1,
                bottomLeftPosition.first + 1 to bottomLeftPosition.second + 1,
                bottomLeftPosition.first + 1 to bottomLeftPosition.second + 2,
                bottomLeftPosition.first + 2 to bottomLeftPosition.second + 1
            )
        }
    }, L {
        override fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>> {
            return (bottomLeftPosition.first until bottomLeftPosition.first + 3).map {
                it to bottomLeftPosition.second
            } + (bottomLeftPosition.second + 1 until bottomLeftPosition.second + 3).map {
                bottomLeftPosition.first + 2 to it
            }
        }
    }, VERTICAL_LINE {
        override fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>> {
            return (bottomLeftPosition.second until bottomLeftPosition.second + 4).map {
                bottomLeftPosition.first to it
            }
        }
    }, SQUARE {
        override fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>> {
            return (bottomLeftPosition.first until bottomLeftPosition.first + 2).flatMap { first ->
                (bottomLeftPosition.second until bottomLeftPosition.second + 2).map { second ->
                    first to second
                }
            }
        }
    };

    abstract fun getPoints(bottomLeftPosition: Pair<Long, Long>): List<Pair<Long, Long>>
}

enum class Movement {
    LEFT, RIGHT
}

fun main() {

    fun parseMovements(input: List<String>) = input.first().map {
        if (it == '<') {
            Movement.LEFT
        } else {
            Movement.RIGHT
        }
    }

    data class Data(
        val rockNumber: Long,
        val rockType: RockType,
        val movementIndex: Int,
        val towerHeight: Long
    )

    fun findCycle(data: List<Data>): Pair<Long, Long>? {
        if (data.size < 2) {
            return null
        }
        var tortoise = 0
        var hare = 1
        while (hare < data.size) {
            val tortoiseData = data[tortoise]
            val hareData = data[hare]

            if (tortoiseData.rockType == hareData.rockType &&
                tortoiseData.movementIndex == hareData.movementIndex) {
                return tortoiseData.rockNumber to hareData.rockNumber
            }

            tortoise++
            hare += 2
        }
        return null
    }

    fun findTowerSizeForRockNumber(rockNumber: Long, data: List<Data>): Long =
        data.find { it.rockNumber == rockNumber }!!.towerHeight

    fun calculateTowerSize(
        tower: Map<Pair<Long, Long>, Boolean>,
        foundCycle: Pair<Long, Long>?,
        data: List<Data>,
        maxRocks: Long
    ) = if (foundCycle != null) {
        val cycleSize = foundCycle.second - foundCycle.first
        val cycleHeight = findTowerSizeForRockNumber(foundCycle.second, data) -
                findTowerSizeForRockNumber(foundCycle.first, data)
        val cycledRocks = maxRocks - foundCycle.first
        val numCycles = cycledRocks / cycleSize
        val remainingRocks = cycledRocks % cycleSize

        val heightBeforeCycle = findTowerSizeForRockNumber(foundCycle.first - 1, data)
        val heightCycles = numCycles * cycleHeight

        val remainingHeight = findTowerSizeForRockNumber(foundCycle.first - 1 + remainingRocks, data) -
                findTowerSizeForRockNumber(foundCycle.first - 1, data)
        heightBeforeCycle + heightCycles + remainingHeight
    } else {
        tower.maxOf { it.key.second } + 1
    }

    fun play(input: List<String>, maxRocks: Long): Long {
        val movements = parseMovements(input)
        val data = mutableListOf<Data>()
        val tower = mutableMapOf<Pair<Long, Long>, Boolean>()
        var movementIndex = 0
        var rockNumber = 0L
        var foundCycle: Pair<Long, Long>? = null

        while (rockNumber < maxRocks && foundCycle == null) {
            val rockType = RockType.values()[(rockNumber % RockType.values().size).toInt()]
            var bottomLeftPosition = 2L to ((tower.maxOfOrNull { it.key.second } ?: -1L) + 1) + 3

            var stop = false
            while (!stop) {
                val newBottomLeftPosition = when (movements[movementIndex]) {
                    Movement.LEFT -> bottomLeftPosition.copy(first = bottomLeftPosition.first - 1)
                    Movement.RIGHT -> bottomLeftPosition.copy(first = bottomLeftPosition.first + 1)
                }

                if (rockType.getPoints(newBottomLeftPosition).none {
                        it in tower ||
                        it.first < 0 ||
                        it.first >= CHAMBER_WIDTH
                }) {
                    bottomLeftPosition = newBottomLeftPosition
                }

                val newFallenBottomLeftPosition = bottomLeftPosition.copy(second = bottomLeftPosition.second - 1)
                if (rockType.getPoints(newFallenBottomLeftPosition).none {
                        it in tower ||
                        it.second < 0
                }) {
                    bottomLeftPosition = newFallenBottomLeftPosition
                } else {
                    val points = rockType.getPoints(bottomLeftPosition)
                    points.forEach {
                        tower[it] = true
                    }
                    stop = true

                    data.add(Data(rockNumber, rockType, movementIndex, tower.maxOf { it.key.second } + 1))
                    foundCycle = findCycle(data)
                }

                movementIndex = (movementIndex + 1) % movements.size
            }
            rockNumber++
        }

        return calculateTowerSize(tower, foundCycle, data, maxRocks)
    }

    fun part1(input: List<String>) = play(input, MAX_ROCKS_PART_ONE)

    fun part2(input: List<String>) = play(input, MAX_ROCKS_PART_TWO)

    val testInput = readInput("Day17_test")
    check(part1(testInput) == 3068L)
    check(part2(testInput) == 1514285714288L)

    val input = readInput("Day17")
    println(part1(input)) // 3157
    println(part2(input)) // 1581449275319
}