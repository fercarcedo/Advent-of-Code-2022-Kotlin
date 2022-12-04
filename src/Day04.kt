val IntRange.size
    get() = if (last < first) {
        0
    } else {
        last - first + 1
    }
fun main() {
    fun calculateRangeIntersection(input: List<String>,
                                   intersectionPredicate: (Set<Int>, IntRange, IntRange) -> Boolean) = input.map { line ->
        val (firstRange, secondRange) = line.split(",").map {
            val parts = it.split("-").map { number -> number.toInt() }
            (parts[0]..parts[1])
        }
        intersectionPredicate(firstRange intersect secondRange, firstRange, secondRange)
    }.count { it }

    fun part1(input: List<String>) = calculateRangeIntersection(input) { intersection, firstRange, secondRange ->
        intersection.size == minOf(firstRange.size, secondRange.size)
    }

    fun part2(input: List<String>) = calculateRangeIntersection(input) { intersection, _, _ ->
        intersection.isNotEmpty()
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println(part1(input)) // 509
    println(part2(input)) // 870
}
