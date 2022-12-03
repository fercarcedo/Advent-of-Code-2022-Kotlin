private const val FIRST_LOWERCASE_PRIORITY = 1
private const val FIRST_UPPERCASE_PRIORITY = 27

fun List<String>.calculateCommonItemsPriorities() = map { it.toSet() }
    .intersect()
    .map { it.priority }

fun <T> List<Set<T>>.intersect() = fold(this[0]) { acc, next ->
    acc intersect next
}

val Char.priority: Int
    get() = if (isUpperCase()) {
        this - 'A' + FIRST_UPPERCASE_PRIORITY
    } else {
        this - 'a' + FIRST_LOWERCASE_PRIORITY
    }

fun main() {

    fun play(groups: List<List<String>>) = groups.map {
        it.calculateCommonItemsPriorities()
    }.sumOf { it.sum() }

    fun part1(input: List<String>) = play(input.map { it.chunked(it.length / 2) })

    fun part2(input: List<String>) = play(input.chunked(3))

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println(part1(input)) // 7878
    println(part2(input)) // 2760
}
