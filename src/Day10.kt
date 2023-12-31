import Day10.PipeType.*
import java.io.File

object Day10 {

    private val maze: Maze = Maze()
    private var startingPoint: Pipe? = null

    fun process(filePath: String) {
        println("$filePath")
        val file = File(filePath)
        buildMaze(file)
        val foundStartingPipe = startingPoint ?: throw RuntimeException("Starting point should be extracted now")
        //println("foundStartingPipe= $foundStartingPipe")
        updateStartingPipe(foundStartingPipe)
        //println("updated foundStartingPipe= $foundStartingPipe")

        val completeLoopSize = travelCompleteMaze(foundStartingPipe)
        //println("maze== $maze")

        val farthestDistance = computeFarthestPointDistance(completeLoopSize)
        println("process1=$farthestDistance")

        val result2 = ScanningMaze(maze, file).scanFile()
        println("process2=$result2")
    }

    private fun buildMaze(file: File) {
        file.readLines().forEachIndexed { lineIdx, line ->
            extractPipeFromLine(lineIdx, line)
        }
    }

    private fun extractPipeFromLine(lineIdx: Int, line: String) {
        line.toList().forEachIndexed { charIdx: Int, char: Char ->
            val pipeType = PipeType.findFromSymbol(char.toString())
            val pipe = Pipe(charIdx, lineIdx, pipeType)
            maze.addPipe(pipe)
            if (pipe.pipeType == UNKNOWN) {
                startingPoint = pipe
            }
        }
    }

    private fun updateStartingPipe(start: Pipe) {
        val pipeType = findUnknownPipeType(start);
        start.pipeType = pipeType
        val startPipeFromMaze: Pipe =
            maze.mapMaze.get(start.key()) ?: throw RuntimeException("Start pipe should be in Maze ${start.key()}")
        startPipeFromMaze.pipeType = pipeType
    }

    private fun findUnknownPipeType(unknownPipe: Pipe): PipeType {
        // Surrounding points
        val north: Pipe? = maze.getPipeByCoord(unknownPipe.abs, unknownPipe.ord - 1)
        val south: Pipe? = maze.getPipeByCoord(unknownPipe.abs, unknownPipe.ord + 1)
        val east: Pipe? = maze.getPipeByCoord(unknownPipe.abs + 1, unknownPipe.ord)
        val west: Pipe? = maze.getPipeByCoord(unknownPipe.abs - 1, unknownPipe.ord)

        val isNorthConn: Boolean = unknownPipe.isConnected(north)
        val isSouthConn: Boolean = unknownPipe.isConnected(south)
        val isEastConn: Boolean = unknownPipe.isConnected(east)
        val isWestConn: Boolean = unknownPipe.isConnected(west)

        println(
            "north=$north south=$south east=$east west=$west " +
                    " - isNorthConn=$isNorthConn isSouthConn=$isSouthConn isEastConn=$isEastConn isWestConn=$isWestConn"
        )
        var result: PipeType

        if (isNorthConn && isSouthConn) {
            result = NORTH_SOUTH
        } else if (isNorthConn && isEastConn) {
            result = NORTH_EAST
        } else if (isNorthConn && isWestConn) {
            result = NORTH_EAST
        } else if (isSouthConn && isEastConn) {
            result = SOUTH_EAST
        } else if (isSouthConn && isWestConn) {
            result = SOUTH_WEST
        } else if (isEastConn && isWestConn) {
            result = EAST_WEST
        } else {
            throw RuntimeException("Impossible combination $north $south $east $west")
        }

        return result
    }

    private fun travelCompleteMaze(startingPoint: Pipe): Long {

        var currentPipe: Pipe = startingPoint
        var previousPipe: Pipe? = null
        var loopIsCompleted = false
        var completeLoopSize: Long = 0
        while (!loopIsCompleted) {
            completeLoopSize++
            val currentConnectedKeys: Pair<Coord, Coord> = currentPipe.getConnectedKeys()
                ?: throw RuntimeException("Invalid case : travelling maze should not lead to a not connected pipe")
            currentPipe.setPartOfTheLoop()
            var nextPipe: Pipe
            // By convention if it's the starting point we take the first connection

            //println("previousPipe=$previousPipe - currentPipe=$currentPipe")
            if (previousPipe == null) {
                nextPipe = maze.getPipeByKey(currentConnectedKeys.first)

                // If it's not the first we take the different connection than the previous one to avoid going backward
            } else {
                if (previousPipe.key() == currentConnectedKeys.first) {
                    nextPipe = maze.getPipeByKey(currentConnectedKeys.second)
                } else if (previousPipe.key() == currentConnectedKeys.second) {
                    nextPipe = maze.getPipeByKey(currentConnectedKeys.first)
                } else {
                    throw RuntimeException("Invalid: current pipe $currentPipe not connected to the previous $previousPipe")
                }
            }

            // move :
            previousPipe = currentPipe
            currentPipe = nextPipe

            // check is completed
            loopIsCompleted = currentPipe.key() == startingPoint.key()
        }

        return completeLoopSize
    }

    private fun computeFarthestPointDistance(completeLoopSize: Long): Long {
        val half: Double = completeLoopSize.toDouble() / 2
        return Math.ceil(half).toLong()
    }

    private class Maze {
        val mapMaze: MutableMap<Coord, Pipe> = mutableMapOf()

        fun addPipe(pipe: Pipe) {
            mapMaze.put(pipe.key(), pipe)
        }

        fun getPipeByCoord(abs: Int, ord: Int): Pipe? {
            return mapMaze.get(Coord(abs, ord))
        }

        fun getPipeByKey(key: Coord): Pipe {
            return mapMaze.get(key) ?: throw RuntimeException("Not existing coord $key")
        }

        override fun toString(): String {
            return mapMaze.toString()
        }
    }

    private data class Pipe(val abs: Int, val ord: Int, var pipeType: PipeType) {

        private var partOfTheLoop: Boolean = false
        private val eastConnectedTypes: Set<PipeType> = setOf(EAST_WEST, NORTH_EAST, SOUTH_EAST)
        private val westConnectedTypes: Set<PipeType> = setOf(EAST_WEST, NORTH_WEST, SOUTH_WEST)

        fun key(): Coord {
            return Coord(abs, ord)
        }

        fun setPartOfTheLoop() {
            partOfTheLoop = true
        }

        fun isPartOfTheLoop(): Boolean {
            return partOfTheLoop
        }

        private fun createConnKey(abs: Int, ord: Int): Coord {
            return Coord(abs, ord)
        }

        fun getConnectedKeys(): Pair<Coord, Coord>? {
            val result = when (pipeType) {
                NORTH_SOUTH -> Pair(createConnKey(abs, ord - 1), createConnKey(abs, ord + 1))
                EAST_WEST -> Pair(createConnKey(abs + 1, ord), createConnKey(abs - 1, ord))
                NORTH_EAST -> Pair(createConnKey(abs, ord - 1), createConnKey(abs + 1, ord))
                NORTH_WEST -> Pair(createConnKey(abs, ord - 1), createConnKey(abs - 1, ord))
                SOUTH_WEST -> Pair(createConnKey(abs, ord + 1), createConnKey(abs - 1, ord))
                SOUTH_EAST -> Pair(createConnKey(abs, ord + 1), createConnKey(abs + 1, ord))
                GROUND -> null
                UNKNOWN -> null
                INSIDE -> null
                OUTSIDE -> null
            }
            return result
        }

        fun isConnected(knownPipe: Pipe?): Boolean {
            val unknownPipeKey = key()
            val knownPipeKey: Pair<Coord, Coord>? = knownPipe?.getConnectedKeys()

            if (knownPipeKey != null) {
                return knownPipeKey.first == unknownPipeKey || knownPipeKey.second == unknownPipeKey
            }

            return false
        }

        /**
         * Hypothetically connectable considering the orientation, bur necessarly besides and connected
         * */
        fun isConnectableFromWest(westPipe: Pipe?): Boolean {
            if (westPipe != null) {
                return isWestConnected() && westPipe.isEastConnected()
            }
            return false
        }

        fun isSymetric(otherPipe: Pipe?): Boolean {
            return ((pipeType == NORTH_EAST && otherPipe?.pipeType == NORTH_WEST)
                    || (pipeType == NORTH_WEST && otherPipe?.pipeType == NORTH_EAST)
                    || (pipeType == SOUTH_EAST && otherPipe?.pipeType == SOUTH_WEST)
                    || (pipeType == SOUTH_WEST && otherPipe?.pipeType == SOUTH_EAST))
        }

        fun isEastConnected(): Boolean {
            return return eastConnectedTypes.contains(pipeType)
        }

        fun isWestConnected(): Boolean {
            return return westConnectedTypes.contains(pipeType)
        }

        override fun toString(): String {
            return "POS PIPE: line=${ord + 1} col=${abs + 1} $pipeType partOfTheLoop=$partOfTheLoop"
        }
    }

    private enum class PipeType(val symbol: String) {
        NORTH_SOUTH("|"),
        EAST_WEST("-"),
        NORTH_EAST("L"),
        NORTH_WEST("J"),
        SOUTH_WEST("7"),
        SOUTH_EAST("F"),
        GROUND("."),
        UNKNOWN("S"),
        INSIDE("I"),
        OUTSIDE("O");

        companion object {
            fun findFromSymbol(symbol: String): PipeType {
                val found = enumValues<PipeType>().find { pipeType -> pipeType.symbol == symbol }
                return found ?: throw RuntimeException("symbol invalid for a pipe type: $symbol")
            }
        }
    }

    private data class Coord(val abs: Int, val ord: Int)

    private class ScanningMaze(val maze: Maze, val file: File) {

        private var tileEnclosedCounter: Long = 0

        fun scanFile(): Long {
            tileEnclosedCounter = 0

            file.readLines().forEachIndexed { lineIdx, line ->
                scanLine(lineIdx, line)
            }

            return tileEnclosedCounter

        }

        private fun scanLine(lineIdx: Int, line: String) {
            var insideLoop = false
            var lastLoopTile: Pipe? = null
            line.toList().forEachIndexed { charIdx: Int, char: Char ->
                val currentTile: Pipe = maze.getPipeByKey(Coord(charIdx, lineIdx))
                val isLoopTile: Boolean = currentTile.isPartOfTheLoop()

                println(
                    "currentTile: $currentTile " +
                            "isLoopTile:$isLoopTile " +
                            "isConnectable=${currentTile.isConnectableFromWest(lastLoopTile)} " +
                            "isSymetric: ${currentTile.isSymetric(lastLoopTile)} " +
                            "insideLoop before: $insideLoop"
                )


                if (isLoopTile) {
                    // If connected we need to check the "symetry" of the cnnected tile
                    if (currentTile.isConnectableFromWest(lastLoopTile)) {
                        // A connected symetric will switch. But a connected asymetric will not switch
                        if (currentTile.isSymetric(lastLoopTile)) {
                            // switch inside/outside
                            insideLoop = !insideLoop
                        }
                    } else {
                        // In case of not connected tiles : there is necessarly a switch
                        insideLoop = !insideLoop
                    }

                    if (currentTile.pipeType != EAST_WEST) {
                        // In the particular case where the connection is EAST_WEST (-)
                        // we don't change the last one : the sense of connection is preserved
                        lastLoopTile = currentTile
                    }
                } else {
                    if (insideLoop) {
                        println("++++ insideLoop lastLoopTile: $lastLoopTile -> currentTile: $currentTile")
                        tileEnclosedCounter++
                    }
                    lastLoopTile = null
                }

                println("INSIDE LOOP AFTER $insideLoop")

            }
        }
    }
}
