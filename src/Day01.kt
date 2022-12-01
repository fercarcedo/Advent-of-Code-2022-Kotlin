fun main() {
    fun List<String>.splitByBlanksToInts() = this.fold(mutableListOf(mutableListOf<Int>())) { acc, next ->
        if (next.isNotBlank()) {
            acc[acc.lastIndex].add(next.toInt())
        } else {
            acc.add(mutableListOf())
        }
        acc
    }

    fun part1(input: List<String>) = input.splitByBlanksToInts()
        .maxOfOrNull { it.sum() }

    fun part2(input: List<String>) = input.splitByBlanksToInts()
        .map { it.sum() }
        .sortedDescending()
        .take(3)
        .sum()

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
