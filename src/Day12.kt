fun main() {

    fun transformElevation(elevation: Char) = when(elevation) {
        'S' -> 'a'
        'E' -> 'z'
        else -> elevation
    }

    fun checkElevation(from: Char, to: Char) = transformElevation(to) - transformElevation(from) <= 1

    fun adjacent(node: Pair<Int, Int>, grid: List<CharArray>): Set<Pair<Int, Int>> {
        val adjacentList = mutableSetOf<Pair<Int, Int>>()
        if (node.first + 1 < grid.size && checkElevation(grid[node.first][node.second], grid[node.first + 1][node.second])) {
            adjacentList.add(node.first + 1 to node.second)
        }
        if (node.first - 1 >= 0 && checkElevation(grid[node.first][node.second], grid[node.first - 1][node.second])) {
            adjacentList.add(node.first - 1 to node.second)
        }
        if (node.second + 1 < grid[node.first].size && checkElevation(grid[node.first][node.second], grid[node.first][node.second + 1])) {
            adjacentList.add(node.first to node.second + 1)
        }
        if (node.second - 1 >= 0 && checkElevation(grid[node.first][node.second], grid[node.first][node.second - 1])) {
            adjacentList.add(node.first to node.second - 1)
        }
        return adjacentList
    }

    fun bfs(startingNode: Pair<Int, Int>,
            endNode: Pair<Int, Int>,
            grid: List<CharArray>,
            visitedNodes: MutableSet<Pair<Int, Int>>,
            distancesMap: MutableMap<Pair<Int, Int>, Int>,
            parentsMap: MutableMap<Pair<Int, Int>, Pair<Int, Int>>): Pair<Int, Int>? {

        val queue = ArrayDeque<Pair<Int, Int>>()
        for (i in grid.indices) {
            for (j in 0 until grid[i].size) {
                distancesMap[i to j] = Integer.MAX_VALUE
            }
        }
        queue.add(startingNode)
        distancesMap[startingNode] = 0
        visitedNodes.add(startingNode)

        while (!queue.isEmpty()) {
            val node = queue.removeFirst()
            if (node == endNode) {
                return node
            }

            for (edge in adjacent(node, grid)) {
                if (!visitedNodes.contains(edge)) {
                    visitedNodes.add(edge)
                    distancesMap[edge] = distancesMap[node]!! + 1
                    parentsMap[edge] = node
                    queue.add(edge)
                }
            }
        }

        return null
    }

    fun part1(input: List<String>): Int {
        val grid = input.map {
            it.toCharArray()
        }

        val visitedNodes = mutableSetOf<Pair<Int, Int>>()
        val distancesMap = mutableMapOf<Pair<Int, Int>, Int>()
        val parentsMap = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()

        val startPosition = grid.mapIndexed { index, it -> index to it.indexOf('S') }.first { it.second >= 0 }
        val endPosition = grid.mapIndexed { index, it -> index to it.indexOf('E') }.first { it.second >= 0 }

        bfs(startPosition, endPosition, grid, visitedNodes, distancesMap, parentsMap)

        return distancesMap[endPosition]!!
    }

    fun part2(input: List<String>): Int {
        val grid = input.map {
            it.toCharArray()
        }

        val startPositions = grid.flatMapIndexed { index, it -> it.mapIndexed { i, c -> index to if (c == 'S' || c == 'a') i else -1 } }
            .filter { it.second >= 0 }

        val endPosition = grid.mapIndexed { index, it -> index to it.indexOf('E') }.first { it.second >= 0 }

        var minDistance = Integer.MAX_VALUE
        for (startPosition in startPositions) {
            val visitedNodes = mutableSetOf<Pair<Int, Int>>()
            val distancesMap = mutableMapOf<Pair<Int, Int>, Int>()
            val parentsMap = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
            bfs(startPosition, endPosition, grid, visitedNodes, distancesMap, parentsMap)
            val distance = distancesMap[endPosition]!!
            if (distance < minDistance) {
                minDistance = distance
            }
        }

        return minDistance
    }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}