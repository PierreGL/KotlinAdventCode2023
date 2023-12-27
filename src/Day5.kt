import java.io.File

class Day5 {

    // extract ranges by mappingRule

    // process rule by rule in the right order
    // ALGO BY RULE

    // extract the lowest

    // ALGO BY RULE
    // extract values
    // find the matched range (if there is)
    // find the position in the range
    // apply the value to the dest / or the default value


    // PROCESS 2
    // Cut in list of slice EACH slice must processed again



    private val mapMappingRule: MutableMap<MappingRule, MutableList<MappingRuleRange>> = mutableMapOf()


    fun process1(filePath: String) {

        println("$filePath")
        var seedslist: List<Long> = mutableListOf()


        var currentMappingRule: MappingRule = MappingRule.SEED_TO_SOIL

        File(filePath).readLines().forEachIndexed { idx, line ->

            if (idx == 0) {
                seedslist = extractSeed(line)
                println(seedslist)

                val seedPairs = extractSeedPair2(line)
                println(seedPairs)

            } else {
                if (line.isNotBlank()) {
                    val firstPartString = line.split(" ").get(0)
                    // println(firstPartString)
                    val maybeMappingRule = getMappingRuleFromValue(firstPartString)
                    if (maybeMappingRule != null) {
                        // println("### $maybeMappingRule")
                        currentMappingRule = maybeMappingRule
                    } else {
                        val mappingRuleRange: MappingRuleRange = extractMappingRuleRange(line)
                        val rangeList = mapMappingRule.getOrElse(currentMappingRule) { mutableListOf() }
                        rangeList.add(mappingRuleRange)
                        mapMappingRule[currentMappingRule] = rangeList
                    }
                }
            }
        }

        // rintln("map: $mapMappingRule")

        val locationMin = seedslist.map { seed -> processSeedLocation(seed) }. min()
        println("result = $locationMin")
    }

    private fun extractSeed(line: String): List<Long> {
        val result = line.split(":").get(1).trim()
            .split(" ")
            .map { str -> str.toLong() }
        return result
    }

    private fun getMappingRuleFromValue(value: String): MappingRule? {
        return MappingRule.values().find { mappingRule -> mappingRule.customName == value }
    }

    private fun extractMappingRuleRange(line: String): MappingRuleRange {
        val valueslist = line.split(" ")
        return MappingRuleRange(valueslist.get(0).toLong(), valueslist.get(1).toLong(), valueslist.get(2).toLong())

    }

    private fun processSeedLocation(seed: Long): Long {
        val soil = processRules(MappingRule.SEED_TO_SOIL, seed)
        val fertilizer = processRules(MappingRule.SOIL_TO_FERTILIZER, soil)
        val water = processRules(MappingRule.FERTILIZER_TO_WATER, fertilizer)
        val light = processRules(MappingRule.WATER_TO_LIGHT, water)
        val temp = processRules(MappingRule.LIGHT_TO_TEMP, light)
        val hum = processRules(MappingRule.TEMP_TO_HUMIDITY, temp)
        val loc = processRules(MappingRule.HUMIDITY_TO_LOCATION, hum)
        println("seed: $seed loc: $loc")
        return loc
    }



    private fun processRules(mappingRule: MappingRule, entryNumber: Long): Long {

        val mappingRangeMatched: MappingRuleRange? = mapMappingRule
            .getOrElse(mappingRule, { mutableListOf() })
            .find { range -> matchRange(entryNumber, range) }

        var resultRule: Long = 0
        if (mappingRangeMatched == null) {
            resultRule = entryNumber
        } else {
            resultRule = extractDestFromRange(entryNumber, mappingRangeMatched)
        }

        // println("entryNb: $entryNumber rule: $mappingRule result: $resultRule")
        return resultRule
    }

    private fun matchRange(number: Long, range: MappingRuleRange): Boolean {
        return number >= range.srcRangeStart && number <= (range.srcRangeStart + range.rangeLength - 1)
    }

    private fun extractDestFromRange(number: Long, range: MappingRuleRange): Long {
        return range.destRangeStart + (number - range.srcRangeStart)
    }

    private fun extractSeedPair2(line: String): List<SeedPair> {
        var seedPairs: MutableList<SeedPair> = mutableListOf()

        val allNumbers = line.split(" ")
        allNumbers.forEachIndexed{idx, value ->
            if (isOddNumber(idx)) {
                seedPairs.add(SeedPair(value.toLong(), allNumbers[idx + 1].toLong()))
            }
        }

        return seedPairs
    }

    private fun isOddNumber(nb: Int): Boolean {
        return (nb % 2) == 1
    }

//    private fun processSeedLocation2(seed: SeedSlice): List<SeedSlice> {
//        val soilList = processRules2(MappingRule.SEED_TO_SOIL, seed)
//        val fertilizerList = soilList
//            .map { soil -> processRules2(MappingRule.SOIL_TO_FERTILIZER, soil) }
//            .flatten()
//        val water = processRules2(MappingRule.FERTILIZER_TO_WATER, fertilizer)
//        val light = processRules2(MappingRule.WATER_TO_LIGHT, water)
//        val temp = processRules2(MappingRule.LIGHT_TO_TEMP, light)
//        val hum = processRules2(MappingRule.TEMP_TO_HUMIDITY, temp)
//        val loc = processRules2(MappingRule.HUMIDITY_TO_LOCATION, hum)
//        println("seed: $seed loc: $loc")
//        return loc
//    }

    private fun processRules2(mappingRule: MappingRule, entryNumber: SeedSlice): List<SeedSlice> {

        val mappingRangeMatched: MappingRuleRange? = mapMappingRule
            .getOrElse(mappingRule, { mutableListOf() })
            .find { range -> matchRange(entryNumber, range) }

        var resultRule: Long = 0
        if (mappingRangeMatched == null) {
            resultRule = entryNumber
        } else {
            resultRule = extractDestFromRange(entryNumber, mappingRangeMatched)
        }

        // println("entryNb: $entryNumber rule: $mappingRule result: $resultRule")
        return resultRule
    }


    private enum class MappingRule(val customName: String) {
        SEED_TO_SOIL("seed-to-soil"),
        SOIL_TO_FERTILIZER("soil-to-fertilizer"),
        FERTILIZER_TO_WATER("fertilizer-to-water"),
        WATER_TO_LIGHT("water-to-light"),
        LIGHT_TO_TEMP("light-to-temperature"),
        TEMP_TO_HUMIDITY("temperature-to-humidity"),
        HUMIDITY_TO_LOCATION("humidity-to-location");

    }

    private data class MappingRuleRange(val destRangeStart: Long, val srcRangeStart: Long, val rangeLength: Long)


    private data class SeedPair(val seedStart: Long, val seedRange: Long)

    private data class SeedSlice(val sliceStart: Long, val sliceRange: Long)

}
