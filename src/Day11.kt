import java.io.File

object Day11 {

    private val observableUniverse: ObservableUniverse = ObservableUniverse

    fun process(filePath: String) {
        println("Day11: $filePath")

        val file = File(filePath)
        val galaxyList: List<Galaxy> = findAllGalaxies(file)
        println("galaxyList: $galaxyList")
        val allDistSum = measureAllGalaxyDistances(galaxyList).sum()

        println("Day11 result1: $allDistSum")

    }

    private fun findAllGalaxies(file: File): List<Galaxy> {
        val galaxiesList: MutableList<Galaxy> = mutableListOf()
        var name = 0
        file.readLines().forEachIndexed { lineIdx: Int, line: String ->
            line.toList().forEachIndexed { charIdx: Int, char: Char ->
                if (isGalaxy(char)) {
                    name++
                    val posColumn = charIdx.toLong()
                    val posLine = lineIdx.toLong()
                    val galaxy = Galaxy(name, posColumn, posLine)
                    galaxiesList.add(galaxy)
                    observableUniverse.galaxyFound(posColumn, posLine)
                }
            }
        }

        return galaxiesList
    }

    private fun isGalaxy(symbol: Char): Boolean {
        return symbol == '#'
    }

    private fun measureAllGalaxyDistances(galaxyList: List<Galaxy>): List<Long> {
        val allGalaxyDistance: MutableList<Long> = mutableListOf()
        println("---")
        galaxyList.forEachIndexed { galaxyId: Int, galaxy: Galaxy ->
            for (otherGalaxyId: Int in galaxyId + 1..<galaxyList.size) {
                val otherGalaxy: Galaxy = galaxyList.get(otherGalaxyId)
                val dist: Long = measureGalaxyDistance(galaxy, otherGalaxy)
                println("$galaxy => $otherGalaxy : $dist")
                allGalaxyDistance.add(dist)
            }
        }

        return allGalaxyDistance
    }

    private fun measureGalaxyDistance(oneGalaxy: Galaxy, otherGalaxy: Galaxy): Long {
        return measureGalaxyDistanceAbs(oneGalaxy, otherGalaxy) + measureGalaxyDistanceOrd(oneGalaxy, otherGalaxy)
    }

    private fun measureGalaxyDistanceAbs(oneGalaxy: Galaxy, otherGalaxy: Galaxy): Long {
        val absMin = Math.min(oneGalaxy.abs, otherGalaxy.abs)
        val absMax = Math.max(oneGalaxy.abs, otherGalaxy.abs)

        var absDist: Long = 0
        for (column: Long in absMin + 1..absMax) {
            if (observableUniverse.isExtendedColumn(column)) {
                absDist += 1000_000
            } else {
                absDist++
            }
        }

        println("oneGalaxy=$oneGalaxy otherGalaxy=$otherGalaxy absMin=$absMin absMax=$absMax absDist=$absDist")

        return absDist
    }

    private fun measureGalaxyDistanceOrd(oneGalaxy: Galaxy, otherGalaxy: Galaxy): Long {
        val ordMin = Math.min(oneGalaxy.ord, otherGalaxy.ord)
        val ordMax = Math.max(oneGalaxy.ord, otherGalaxy.ord)

        var ordDist: Long = 0
        for (column: Long in ordMin + 1..ordMax) {
            if (observableUniverse.isExtendedLine(column)) {
                ordDist += 1000_000
            } else {
                ordDist++

            }
        }

        return ordDist
    }

    private data class Galaxy(val name: Int, val abs: Long, val ord: Long)

    private object ObservableUniverse {
        val mapColumnWithGalaxy: MutableMap<Long, Boolean> = mutableMapOf()
        val mapLineWithGalaxy: MutableMap<Long, Boolean> = mutableMapOf()

        fun galaxyFound(abs: Long, ord: Long) {
            mapColumnWithGalaxy.put(abs, true)
            mapLineWithGalaxy.put(ord, true)
        }

        fun isExtendedColumn(column: Long): Boolean {
            return !mapColumnWithGalaxy.contains(column)
        }

        fun isExtendedLine(line: Long): Boolean {
            return !mapLineWithGalaxy.contains(line)
        }
    }
}