import java.nio.file.Paths

private const val MAX_DIRECTORY_TOTAL_SIZE = 100000
private const val TOTAL_DISK_SPACE = 70000000
private const val SPACE_NEEDED_FOR_UPDATE = 30000000

private val CD_COMMAND_REGEX = "\\$ cd (?<directory>.+)".toRegex()
private val LS_COMMAND_REGEX = "\\$ ls".toRegex()
private val DIR_REGEX = "dir (?<directory>.+)".toRegex()
private val FILE_REGEX = "(?<size>\\d+) (?<name>.+)".toRegex()

fun String.toAbsolutePath(parentDir: String) = if (Paths.get(this).isAbsolute) {
    this
} else if (".." == this) {
    Paths.get(parentDir).parent.toString()
} else {
    Paths.get(parentDir).resolve(Paths.get(this)).toString()
}

sealed interface Command {
    object ListCommand: Command {
        override fun execute(context: InterpreterContext): InterpreterContext {
            context.fileTree.addDirectory(context.currentDirectory)
            return context.copy(listingDirectory = true)
        }
    }

    data class ChangeDirectoryCommand(val directory: String): Command {
        override fun execute(context: InterpreterContext): InterpreterContext {
            return context.copy(
                currentDirectory = directory.toAbsolutePath(context.currentDirectory),
                listingDirectory = false
            )
        }
    }

    fun execute(context: InterpreterContext): InterpreterContext
}

data class InterpreterContext(
    val currentDirectory: String,
    val listingDirectory: Boolean,
    val fileTree: FileTree
)

class Interpreter {
    private var context = InterpreterContext(
        currentDirectory = "/",
        listingDirectory = false,
        fileTree = FileTree()
    )

    val listingDirectory: Boolean
        get() = context.listingDirectory

    fun execute(command: Command) {
        context = command.execute(context)
    }

    fun registerFile(name: String, size: Int) =
        context.fileTree.addFile(name.toAbsolutePath(context.currentDirectory), size)

    fun registerDirectory(name: String) =
        context.fileTree.addDirectory(name.toAbsolutePath(context.currentDirectory))

    fun calculateSizesMap(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        NodeSizeVisitor().visit(result, context.fileTree.root as FileTreeNode.DirectoryNode)
        return result
    }
}

class NodeSizeVisitor {
    fun visit(map: MutableMap<String, Int>, node: FileTreeNode.DirectoryNode): Int {
        val size = node.children.sumOf {
            when (it) {
                is FileTreeNode.FileNode -> it.size
                is FileTreeNode.DirectoryNode -> visit(map, it)
            }
        }
        map[node.name] = size
        return size
    }
}

data class FileTree(
    val root: FileTreeNode = FileTreeNode.DirectoryNode("/")
) {
    fun addDirectory(name: String) = addNode(FileTreeNode.DirectoryNode(name))

    fun addFile(name: String, size: Int) = addNode(FileTreeNode.FileNode(name, size))

    private fun addNode(newNode: FileTreeNode) {
        val treeNode = searchNode(newNode.name)
        if (treeNode == null) {
            val parentPath = Paths.get(newNode.name).parent.toString()
            var parentNode = searchNode(parentPath)
            if (parentNode == null) {
                addDirectory(parentPath)
                parentNode = searchNode(parentPath)
            }
            if (parentNode is FileTreeNode.DirectoryNode) {
                parentNode.children.add(newNode)
            }
        }
    }

    private fun searchNode(name: String, node: FileTreeNode = root): FileTreeNode? {
        if (node.name == name) {
            return node
        }

        if (node is FileTreeNode.DirectoryNode) {
            node.children.filterIsInstance<FileTreeNode.DirectoryNode>().forEach {
                val node = searchNode(name, it)
                if (node != null) {
                    return node
                }
            }
        }
        return null
    }
}

sealed class FileTreeNode {
    abstract val name: String

    data class FileNode(override val name: String, val size: Int): FileTreeNode()
    data class DirectoryNode(override val name: String, val children: MutableList<FileTreeNode> = mutableListOf()): FileTreeNode()
}

fun main() {

    fun play(input: List<String>): Map<String, Int> {
        val interpreter = Interpreter()
        for (line in input) {
            if (line.startsWith("$")) {
                val cdCommandMatch = CD_COMMAND_REGEX.matchEntire(line)
                if (cdCommandMatch != null) {
                    interpreter.execute(Command.ChangeDirectoryCommand(cdCommandMatch.groups["directory"]!!.value))
                } else if (LS_COMMAND_REGEX.matches(line)) {
                    interpreter.execute(Command.ListCommand)
                }
            } else if (interpreter.listingDirectory) {
                val dirMatch = DIR_REGEX.matchEntire(line)
                val fileMatch = FILE_REGEX.matchEntire(line)
                if (dirMatch != null) {
                    interpreter.registerDirectory(dirMatch.groups["directory"]!!.value)
                } else if (fileMatch != null) {
                    interpreter.registerFile(fileMatch.groups["name"]!!.value, fileMatch.groups["size"]!!.value.toInt())
                }
            }
        }
        return interpreter.calculateSizesMap()
    }

    fun part1(input: List<String>) = play(input)
        .filterValues { it <= MAX_DIRECTORY_TOTAL_SIZE }
        .map { it.value }
        .sum()

    fun part2(input: List<String>): Int {
        val sizesMap = play(input)
        val totalUsedSpace = sizesMap["/"] ?: 0
        val remainingSpace = TOTAL_DISK_SPACE - totalUsedSpace
        if (remainingSpace < SPACE_NEEDED_FOR_UPDATE) {
            val spaceToBeFreed = SPACE_NEEDED_FOR_UPDATE - remainingSpace
            return sizesMap.map { it.value }
                .filter { it >= spaceToBeFreed }
                .min()

        }
        return 0
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println(part1(input)) // 1325919
    println(part2(input)) // 2050735
}
