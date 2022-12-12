import kotlin.math.floor

private val MONKEY_REGEX = "Monkey\\s+(?<monkeyNumber>\\d+):".toRegex()
private val STARTING_ITEMS_REGEX = "\\s+Starting\\s+items:\\s+(?<items>\\d+(,\\s+\\d+)*)".toRegex()
private val OPERATION_REGEX = "\\s+Operation:\\s+new\\s+=\\s+old\\s+(?<operation>[+*])\\s+(?<value>(\\d+|old))".toRegex()
private val TEST_REGEX = "\\s+Test:\\s+divisible\\s+by\\s+(?<value>\\d+)".toRegex()
private val TEST_IF_REGEX = "\\s+If\\s+(?<result>true|false):\\s+throw\\s+to\\s+monkey\\s+(?<monkeyNumber>\\d+)".toRegex()

data class Monkey(
    val items: MutableList<Long>,
    val operation: (Long) -> Long,
    val test: (Long) -> Boolean,
    val throwMap: Map<Boolean, Int>
)

data class MonkeysResult(
    val monkeys: List<Monkey>,
    val dividends: Set<Long>
)

fun main() {

    fun parseOperation(operation: Char, valueString: String): (Long) -> Long {
        if (operation == '*' && valueString == "old") {
            return { it * it }
        }

        if (operation == '+' && valueString == "old") {
            return { it + it }
        }

        val value = valueString.toLong()
        return if (operation == '+') {
            { it + value }
        } else {
            { it * value }
        }
    }

    fun parseInput(input: List<String>): MonkeysResult {
        val monkeys = mutableListOf<Monkey>()
        var monkeyNumber = 0
        var items: MutableList<Long> = mutableListOf()
        var operationFunction: (Long) -> Long = { it }
        var test: (Long) -> Boolean = { true }
        var throwMap: MutableMap<Boolean, Int> = mutableMapOf()
        val dividends = mutableSetOf<Long>()

        for (line in input) {
            if (line.isBlank()) {
                continue
            }

            if (MONKEY_REGEX.matches(line)) {
                monkeyNumber = MONKEY_REGEX.matchEntire(line)!!.groups["monkeyNumber"]!!.value.toInt()
                throwMap = mutableMapOf()
            } else if (STARTING_ITEMS_REGEX.matches(line)) {
                items = STARTING_ITEMS_REGEX.matchEntire(line)!!.groups["items"]!!.value.split(",").map { it.trim().toLong() }.toMutableList()
            } else if (OPERATION_REGEX.matches(line)) {
                val matchResult = OPERATION_REGEX.matchEntire(line)!!
                val operation = matchResult.groups["operation"]!!.value.first()
                val valueString = matchResult.groups["value"]!!.value
                operationFunction = parseOperation(operation, valueString)
            } else if (TEST_REGEX.matches(line)) {
                val value = TEST_REGEX.matchEntire(line)!!.groups["value"]!!.value.toLong()
                test = { it % value == 0L }
                dividends.add(value)
            } else if (TEST_IF_REGEX.matches(line)) {
                val matchResult = TEST_IF_REGEX.matchEntire(line)!!
                val result = matchResult.groups["result"]!!.value.toBoolean()
                throwMap[result] = matchResult.groups["monkeyNumber"]!!.value.toInt()

                if (!result) {
                    monkeys.add(monkeyNumber, Monkey(items, operationFunction, test, throwMap))
                }
            }
        }

        return MonkeysResult(monkeys, dividends)
    }

    fun play(input: List<String>, partOne: Boolean = true): Long {
        val monkeysResult = parseInput(input)
        val lcmDividends = monkeysResult.dividends.reduce { acc, next -> acc * next }

        val monkeyInspectionsMap: MutableMap<Int, Long> = mutableMapOf()
        val numRounds = if (partOne) 20 else 10000

        for (round in 0 until numRounds) {
            for ((index, monkey) in monkeysResult.monkeys.withIndex()) {
                for (item in monkey.items) {
                    if (index !in monkeyInspectionsMap) {
                        monkeyInspectionsMap[index] = 1
                    } else {
                        monkeyInspectionsMap[index] = monkeyInspectionsMap[index]!! + 1
                    }
                    val worryLevel = if (partOne) {
                        floor(monkey.operation(item) / 3.0).toLong()
                    } else {
                        monkey.operation(item) % lcmDividends
                    }
                    val testResult = monkey.test(worryLevel)
                    val monkeyToThrowTo = monkey.throwMap[testResult]!!
                    monkeysResult.monkeys[monkeyToThrowTo].items.add(worryLevel)
                }
                monkey.items.clear()
            }
        }

        return monkeyInspectionsMap.map { it.value }.sortedDescending().take(2).reduce { acc, next -> acc * next }
    }

    fun part1(input: List<String>) = play(input)

    fun part2(input: List<String>) = play(input, false)

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 10605L)
    check(part2(testInput) == 2713310158L)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
