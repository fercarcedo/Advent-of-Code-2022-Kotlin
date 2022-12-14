import com.beust.klaxon.Klaxon

private val FIRST_DIVIDER = listOf(listOf(2))
private val SECOND_DIVIDER = listOf(listOf(6))

fun <T> MutableList<T>.swap(from: Int, to: Int) {
    val temp = this[from]
    this[from] = this[to]
    this[to] = temp
}

fun main() {

    fun inRightOrder(left: Any, right: Any, index: Int, resultsMap: MutableMap<Int, Boolean>) {
        if (left is Int && right is Int) {
            if (left < right) {
                resultsMap.putIfAbsent(index, true)
            }
            if (left > right) {
                resultsMap.putIfAbsent(index, false)
            }
        } else if (left is List<*> && right is List<*>) {
            for (i in 0 until maxOf(left.size, right.size)) {
                if (i >= left.size) {
                    resultsMap.putIfAbsent(index, true)
                }
                if (i >= right.size) {
                    resultsMap.putIfAbsent(index, false)
                }
                if (index !in resultsMap) {
                    inRightOrder(left[i]!!, right[i]!!, index, resultsMap)
                }
            }
        } else if (left is Int) {
            inRightOrder(listOf(left), right, index, resultsMap)
        } else {
            inRightOrder(left, listOf(right), index, resultsMap)
        }
    }

    fun parsePackets(input: List<String>) = input.filter { it.isNotBlank() }
        .map { Klaxon().parseArray<Any>(it)!! }
        .toMutableList()

    fun part1(input: List<String>): Int {
        val pairs = parsePackets(input).chunked(2)
        val resultsMap = mutableMapOf<Int, Boolean>()

        for ((pairIndex, pair) in pairs.withIndex()) {
            inRightOrder(pair[0], pair[1], pairIndex, resultsMap)
        }

        return resultsMap.filterValues { it }
            .map { it.key + 1 }
            .sum()
    }

    fun bubbleSort(packets: MutableList<List<Any>>) {
        for (i in 0 until packets.size - 1) {
            for (j in 0 until packets.size - i - 1) {
                val resultsMap = mutableMapOf<Int, Boolean>()
                inRightOrder(packets[j], packets[j + 1], 0, resultsMap)
                if (!resultsMap[0]!!) {
                    packets.swap(j, j + 1)
                }
            }
        }
    }

    fun part2(input: List<String>): Int {
        val packets = parsePackets(input)

        packets.add(FIRST_DIVIDER)
        packets.add(SECOND_DIVIDER)

        bubbleSort(packets)

        return (packets.indexOf(FIRST_DIVIDER) + 1) * (packets.indexOf(SECOND_DIVIDER) + 1)
    }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println(part1(input)) // 6101
    println(part2(input)) // 21909
}