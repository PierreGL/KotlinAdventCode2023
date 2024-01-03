import java.io.File
import java.lang.RuntimeException
import java.util.stream.Collectors

class Day08 {

    private var endPositionValue: String = "ZZZ"
    private var instructions: List<String> = listOf()

    fun process(filePath: String) {
        println("$filePath")

        instructions = extractInstructions(filePath)
        val positions: List<Position> = extractPositions(filePath)
//        println("instructions: $instructions")
//        println("positions: $positions")
//        println("positions size: ${positions.size}")
        val positionsMap = convertPositionsToMap(positions)

        process1(positionsMap)
        process2(positionsMap, positions)
    }

    private fun process1(positionsMap: Map<String, Position>) {
        println("--------")
        val instructionsSet = InstructionsSet(instructions)
        val initialPosition: Position? = positionsMap.get("AAA")
        if (initialPosition != null) {
            val result = navigateNetwork(instructionsSet, initialPosition, positionsMap)
            println("process1 result: $result")
        } else {
            throw RuntimeException("Initial position not found")
        }
    }

    private fun process2(positionsMap: Map<String, Position>, positions: List<Position>) {
        println("--------")
        val initialPositions: List<Position> = extractInitialPositions2(positions)
        println("initialPositions: $initialPositions")
        val result = navigateNetwork2(initialPositions, positionsMap)
        println("process2 result: $result")
    }

    private fun extractInstructions(filePath: String): List<String> {
        val firstLine = File(filePath).readLines().first()
        val instructions: List<String> = firstLine.toList().map { char: Char -> char.toString() }
        println("instructions size: ${instructions.size}")
        return instructions
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

        val regex: Regex = "[()]".toRegex()
        val leftPartStrClean: String = leftPartStr.replace(regex, "")
        val nextPosList = leftPartStrClean.split(",")
        val nextPosLeft = nextPosList.get(0).trim()
        val nextPosRight = nextPosList.get(1).trim()

        return Position(positionValue, nextPosLeft, nextPosRight)
    }

    private fun isEndPosition(position: Position): Boolean {
        return position.value == endPositionValue
    }

    private fun convertPositionsToMap(positions: List<Position>): Map<String, Position> {
        return positions.stream().collect(Collectors.toMap({ pos -> pos.value }, { pos -> pos }))
    }

    private fun navigateNetwork(
        instructionsSet: InstructionsSet,
        initialPosition: Position,
        positionsMap: Map<String, Position>
    ): Long {

        var currentPosition: Position = initialPosition
        var stepCounter: Long = 0

        var finalPositionFound: Boolean = false

        while (!finalPositionFound) {
            stepCounter++
            val nextInstruction = instructionsSet.nextInstruction()

            val nextPositionValue: String = currentPosition.getNextPositionValue(nextInstruction.value)
            val nextPosition: Position? = positionsMap.get(nextPositionValue)

            if (nextPosition != null) {
                currentPosition = nextPosition
            } else {
                throw RuntimeException("Next position not found")
            }

            if (isEndPosition(currentPosition)) {
                println("reach final position: $currentPosition")
                finalPositionFound = true
            }
        }

        return stepCounter
    }

    private fun extractInitialPositions2(allPositions: List<Position>): List<Position> {
        return allPositions.filter { position -> position.isInitialPosition() }
    }

    private fun navigateNetwork2(initialPositions: List<Position>, positionsMap: Map<String, Position>): Long {

        val navigationPatterns: List<NavigationPattern> =
            initialPositions.map { initialPosition -> computeNavigationPattern2(initialPosition, positionsMap) }

        val stepPatternListSorted: List<Long> =
            navigationPatterns.map { navigationPattern -> navigationPattern.stepPattern }.sortedDescending()

        println(navigationPatterns)
        println(stepPatternListSorted)

        val result = findFirstHighestMatchAll(stepPatternListSorted)

        return result
    }

    private fun findFirstHighestMatchAll(stepPatternListSorted: List<Long>): Long {
        val highest = stepPatternListSorted.get(0)
        val others: List<Long> = stepPatternListSorted.subList(1, stepPatternListSorted.size)

        var allMatchFound = false
        var multiplierIndex = 1
        var result: Long = 0
        while (!allMatchFound) {
            val currentHighest = highest * multiplierIndex
            allMatchFound = allMatchHighest(currentHighest, others)
            multiplierIndex++

            if (allMatchHighest(currentHighest, others)) {
                allMatchFound = true
                result = currentHighest
            } else {
                multiplierIndex++
            }
        }

        return result
    }


    private fun allMatchHighest(highest: Long, others: List<Long>): Boolean {
        return others.all { other -> highestMatch(highest, other) }
    }

    private fun highestMatch(highest: Long, other: Long): Boolean {
        return highest % other == 0L
    }

    private fun nextPosition2(
        currentPosition: Position,
        nextInstruction: String,
        positionsMap: Map<String, Position>
    ): Position {
        val nextPositionValue: String = currentPosition.getNextPositionValue(nextInstruction)
        val nextPosition: Position? = positionsMap.get(nextPositionValue)
        if (nextPosition != null) {
            return nextPosition
        } else {
            throw RuntimeException("Next position not found")
        }
    }

    private fun allEndPosition2(allCurrentPositions: List<Position>, stepCounter: Long): Boolean {

        val areFound: List<Boolean> = allCurrentPositions.map { position -> position.isEndPosition() }
        val nbFound = areFound.filter { position -> position }.size
        if (nbFound > 2) {
            println("stepCounter: $stepCounter")
            println("allCurrentPositions: $allCurrentPositions")
            println("areFound=$areFound nbFound=$nbFound")
        }


        return allCurrentPositions.all { position -> position.isEndPosition() }
    }

    private fun computeNavigationPattern2(
        initialPosition: Position,
        positionsMap: Map<String, Position>
    ): NavigationPattern {

        val navigationPattern = NavigationPattern(initialPosition)
        val instructionsSet = InstructionsSet(instructions)


        var currentPosition: Position = initialPosition
        var stepCounter: Long = 0
        var isPatternCompleted = false

        while (!isPatternCompleted) {
            stepCounter++
            val newInstruction = instructionsSet.nextInstruction()
            val newPositionValue: String = currentPosition.getNextPositionValue(newInstruction.value)
            val newPosition: Position? = positionsMap.get(newPositionValue)

            if (newPosition != null) {
                currentPosition = newPosition
            } else {
                throw RuntimeException("Next position not found")
            }

            if (currentPosition.isEndPosition()) {
                println("currentPosition match end: $currentPosition stepCounter: $stepCounter")
                navigationPattern.stepPattern = stepCounter
                isPatternCompleted = true
            }
        }

        return navigationPattern
    }

    private class InstructionsSet(val instructionsValues: List<String>) {
        var pos: Int = 0
        var currentInstructionValue: String = ""

        fun nextInstruction(): Instruction {
            if (pos >= instructionsValues.size) {
                pos = 0
            }

            val instr = instructionsValues.get(pos)
            currentInstructionValue = instr
            val nextInstruction = Instruction(currentInstructionValue, pos)
            pos++

            return nextInstruction
        }

        override fun toString(): String {
            return instructionsValues.toString()
        }
    }

    private data class Instruction(val value: String, val pos: Int) {
        fun isInitialInstruction(): Boolean {
            return pos == 0
        }
    }

    private data class Position(
        val value: String,
        val nextLeftPos: String,
        val nextRightPos: String
    ) {
        fun getNextPositionValue(instruction: String): String {
            if (instruction == "L") {
                return nextLeftPos
            } else if (instruction == "R") {
                return nextRightPos
            }
            throw RuntimeException("Instruction not valid: [$instruction]")
        }

        fun isInitialPosition(): Boolean {
            return value.substring(2) == "A"
        }

        fun isEndPosition(): Boolean {
            return value.substring(2) == "Z"
        }
    }

    private class NavigationPattern(val initialPosition: Position) {
        /**
         * All the steps matching a potential end ("Z")
         * */
        var matchingStep: MutableList<Long> = mutableListOf()

        /**
         * Number of step required to have the complete pattern and go back to the beginning
         * */
        var stepPattern: Long = 0

        fun addMatchingStep(step: Long) {
            matchingStep.add(step)
        }

        override fun toString(): String {
            return "${initialPosition.value} pattern=$stepPattern"
        }
    }
}
