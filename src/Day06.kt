import java.io.File

class Day06 {
    fun process(filePath: String) {
        println("$filePath")
        process1(filePath)
        process2(filePath)
    }

    private fun process1(filePath: String) {
        val races = extractRaces(filePath)
        val result = races.map { race -> processRace(race).size }.reduce { acc, nbRaceRecord -> acc * nbRaceRecord }
        println("result1: $result")
    }

    private fun extractRaces(filePath: String): List<RaceResult> {
        var times: List<Long> = mutableListOf()
        var distances: List<Long> = mutableListOf()
        File(filePath).readLines().forEachIndexed { idx, line ->
            run {
                if (idx == 0) {
                    times = extractValues(line)
                } else if (idx == 1) {
                    distances = extractValues(line)
                }

            }
        }

        val races = times.mapIndexed { idx, time ->
            val dist = distances.get(idx)
            RaceResult(time, dist)
        }

        println("races: $races")
        return races

    }

    private fun extractValues(line: String): List<Long> {
        val valuesStr = line.split(":").get(1).trim()
        println(valuesStr)
        val valuesList = valuesStr.split(" ")
            .filter { value -> value.isNotBlank() }
            .map { value ->
                //println(value)
                value.trim().toLong()
            }
        println(valuesList)
        return valuesList

    }

    private fun processRace(race: RaceResult): List<RaceTry> {

        var raceTries: MutableList<RaceTry> = mutableListOf()
        for (timePushButton in 1..race.time - 1) {
            val timeTravel = race.time - timePushButton
            val distReached = timePushButton * timeTravel
            val isRecord = distReached > race.distance
            if (isRecord) {
                val raceTry = RaceTry(distReached, isRecord)
                raceTries.add(raceTry)
            }
        }

        return raceTries
    }


    private fun process2(filePath: String) {
        val race: RaceResult = extractRace2(filePath)
        val result2 = processRace(race).size
        println("result2: $result2")
    }

    private fun extractRace2(filePath: String): RaceResult {
        var time: Long = 0
        var distance: Long = 0
        File(filePath).readLines().forEachIndexed { idx, line ->
            run {
                if (idx == 0) {
                    time = extractValue2(line)
                } else if (idx == 1) {
                    distance = extractValue2(line)
                }

            }
        }

        val race = RaceResult(time, distance)

        println("race: $race")
        return race

    }

    private fun extractValue2(line: String): Long {
        val valuesStr = line.split(":").get(1).trim()
        val fomattedValue = valuesStr.replace(" ", "")
        return fomattedValue.toLong()
    }


    private data class RaceResult(val time: Long, val distance: Long)
    private data class RaceTry(val distReached: Long, val isBetter: Boolean)

}
