import java.io.File
import java.util.stream.Collectors

class Day8 {

    fun process(filePath: String) {
        println("$filePath")



        val instructions: List<String> = extractInstructions(filePath)
        val positions: List<Position> = extractPositions(filePath)
        println("instructions: $instructions")
        println("positions: $positions")
        val positionsMap = convertPositionsToMap(positions)

    }



    private fun extractInstructions(filePath: String): List<String> {
        val firstLine = File(filePath).readLines().first()
        return firstLine.toList().map { char:Char -> char.toString() }
    }

    private fun extractPositions(filePath: String): List<Position> {
        val positions: MutableList<Position> = mutableListOf()
        File(filePath).readLines().forEachIndexed { idx, line ->
            if (idx != 0 && idx != 1) {
                val pos: Position = extractPosition(line)
                positions.add(pos)
            }
        }
        return positions
    }

    private fun extractPosition(oneLine: String): Position {
        val lineList = oneLine.split("=")
        val positionValue: String = lineList.get(0).trim()
        val leftPartStr: String = lineList.get(1).trim()
            //.replace("(", "")

        //val characToRemove = listOf('\(', '\\)')
        //val regex = characToRemove.joinToString(separator = "|").toRegex()
        val regex: Regex = "[()]".toRegex()

        val leftPartStrClean:String = leftPartStr.replace(regex, "")
        println("leftPartStrClean: $leftPartStrClean")
        val nextPosList = leftPartStrClean.split(",")
        val nextPosLeft = nextPosList.get(0).trim()
        val nextPosRight = nextPosList.get(1).trim()

        return Position(positionValue, nextPosLeft, nextPosRight)
    }

    private fun convertPositionsToMap(positions: List<Position>): Map<String, Position> {
        return positions.stream().collect(Collectors.toMap({ pos -> pos.value}, {pos -> pos}))
    }

    private enum class Instruction(val label: String) {
        LEFT("L"),
        RIGHT("R");
    }

    private data class Position(val value: String,
                                val nextLeftPos: String,
                                val nextRightPos: String)
}