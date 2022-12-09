data class VisibilityStatus(
    val visible: Boolean,
    val viewingDistance: Int
)

fun main() {

    fun calculateVisibilityStatus(treeHeight: Int, rowRange: Iterable<Int>, colRange: Iterable<Int>, grid: List<List<Int>>): VisibilityStatus {
        var viewingDistance = 0
        for (i in rowRange) {
            for (j in colRange) {
                viewingDistance += 1
                if (grid[i][j] >= treeHeight) {
                    return VisibilityStatus(false, viewingDistance)
                }
            }
        }
        return VisibilityStatus(true, viewingDistance)
    }

    fun calculateVisibilityStatuses(treeRow: Int, treeCol: Int, grid: List<List<Int>>): List<VisibilityStatus> {
        return listOf(calculateVisibilityStatus(grid[treeRow][treeCol], treeRow - 1 downTo 0, (treeCol..treeCol), grid),
                calculateVisibilityStatus(grid[treeRow][treeCol], treeRow + 1 until grid.size, (treeCol..treeCol), grid),
                calculateVisibilityStatus(grid[treeRow][treeCol], (treeRow..treeRow), treeCol - 1 downTo 0, grid),
                calculateVisibilityStatus(grid[treeRow][treeCol], (treeRow..treeRow), treeCol + 1 until grid[treeRow].size, grid))
    }

    fun isVisible(treeRow: Int, treeCol: Int, grid: List<List<Int>>) =
        calculateVisibilityStatuses(treeRow, treeCol, grid).any { it.visible }

    fun calculateViewingDistance(treeRow: Int, treeCol: Int, grid: List<List<Int>>) =
        calculateVisibilityStatuses(treeRow, treeCol, grid).fold(1) { acc, next -> acc * next.viewingDistance }

    fun parseGrid(input: List<String>) = input.map { line -> line.toList().map { it.toString().toInt() } }

    fun part1(input: List<String>): Int {
        val grid = parseGrid(input)
        var countVisible = grid.size * 2 + grid[0].size * 2 - 4
        for (i in 1 until grid.size - 1) {
            for (j in 1 until grid[i].size - 1) {
                if (isVisible(i, j, grid)) {
                    countVisible += 1
                }
            }
        }
        return countVisible
    }

    fun part2(input: List<String>): Int {
        val grid = parseGrid(input)
        var maxViewingDistance = 0
        for (i in 1 until grid.size - 1) {
            for (j in 1 until grid[i].size - 1) {
                val viewingDistance = calculateViewingDistance(i, j, grid)
                if (viewingDistance > maxViewingDistance) {
                    maxViewingDistance = viewingDistance
                }
            }
        }
        return maxViewingDistance
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input)) // 1854
    println(part2(input)) // 527340
}
