import java.io.File
import kotlin.math.min

class Day5 {

    private val mappingRulesWithRanges: MutableMap<MappingRule, List<MappingRuleRange>> = mutableMapOf()


    fun processBoth(filePath: String) {

        println("$filePath")
        var seedslist: List<Long> = mutableListOf()
        var seedSlices: List<ValuesSlice> = mutableListOf()
        var currentMappingRule: MappingRule = MappingRule.SEED_TO_SOIL
        val buildingMapMappingRule: MutableMap<MappingRule, MutableList<MappingRuleRange>> = mutableMapOf()

        File(filePath).readLines().forEachIndexed { idx, line ->

            if (idx == 0) {
                seedslist = extractSeed(line)
                //println(seedslist)

                seedSlices = extractSeedSlice2(line)
                println(seedSlices)
            } else {
                if (line.isNotBlank()) {
                    val firstPartString = line.split(" ").get(0)
                    val maybeMappingRule = getMappingRuleFromValue(firstPartString)
                    if (maybeMappingRule != null) {
                        currentMappingRule = maybeMappingRule
                    } else {
                        val mappingRuleRange: MappingRuleRange = extractMappingRuleRange(line)
                        val rangeList = buildingMapMappingRule.getOrElse(currentMappingRule) { mutableListOf() }
                        rangeList.add(mappingRuleRange)
                        buildingMapMappingRule[currentMappingRule] = rangeList
                    }
                }
            }
        }

        sortAllMappingRuleRanges(buildingMapMappingRule)


        //PROCESS 1
        val locationMin = seedslist.map { seed -> processSeedLocation(seed) }.min()
        //println("process1 result = $locationMin")

        // PROCESS 2
        val locationMin2 = processSeedToLocation2(seedSlices)
            .map { seedSliceLocation -> seedSliceLocation.sliceStart }
            .min()

        println("process2 result = $locationMin2")
    }

    private fun sortAllMappingRuleRanges(buildingMappingRule: MutableMap<MappingRule, MutableList<MappingRuleRange>>) {
        println("---- SORT RANGES ----")
        MappingRule.values().forEach { mappingRule ->
            run {
                val unsortedRangeList = buildingMappingRule.getOrElse(mappingRule) { mutableListOf() }
                val sortedRangeList = unsortedRangeList.sorted()

                println("SORT for RULE : $mappingRule")
                println("unsortedRangeList: $unsortedRangeList")
                println("sortedRangeList: $sortedRangeList")

                mappingRulesWithRanges.put(mappingRule, sortedRangeList)
            }
        }
    }

    private fun extractSeed(line: String): List<Long> {
        val result = line.split(":").get(1).trim().split(" ").map { str -> str.toLong() }
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
        //println("seed: $seed loc: $loc")
        return loc
    }


    private fun processRules(mappingRule: MappingRule, entryNumber: Long): Long {

        val mappingRangeMatched: MappingRuleRange? = mappingRulesWithRanges
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

    private fun extractSeedSlice2(line: String): List<ValuesSlice> {
        var seedSlices: MutableList<ValuesSlice> = mutableListOf()

        val allNumbers = line.split(" ")
        allNumbers.forEachIndexed { idx, value ->
            if (isOddNumber2(idx)) {
                seedSlices.add(ValuesSlice(value.toLong(), allNumbers[idx + 1].toLong()))
            }
        }

        return seedSlices
    }

    private fun isOddNumber2(nb: Int): Boolean {
        return (nb % 2) == 1
    }

    private fun processSeedToLocation2(seedSlices: List<ValuesSlice>): List<ValuesSlice> {
        println("---- PROCESS SEED ----")
        println("seedSlice: $seedSlices")
//        val soilList: List<SeedSlice> = processRules2(MappingRule.SEED_TO_SOIL, seedSlice)

        val soilList: List<ValuesSlice> = seedSlices
            .map { seed -> processRules2(MappingRule.SEED_TO_SOIL, seed) }
            .flatten()
        println("soilList: $soilList")

        val fertilizerList: List<ValuesSlice> = soilList
            .map { soil -> processRules2(MappingRule.SOIL_TO_FERTILIZER, soil) }
            .flatten()
        println("fertilizerList: $fertilizerList")

        val waterList: List<ValuesSlice> = fertilizerList
            .map { fertilizer: ValuesSlice -> processRules2(MappingRule.FERTILIZER_TO_WATER, fertilizer) }
            .flatten()
        println("waterList: $waterList")

        val lightList: List<ValuesSlice> = waterList
            .map { water -> processRules2(MappingRule.WATER_TO_LIGHT, water) }
            .flatten()
        println("lightList: $lightList")

        val tempList = lightList
            .map { light -> processRules2(MappingRule.LIGHT_TO_TEMP, light) }
            .flatten()
        println("tempList: $tempList")

        val humidityList = tempList
            .map { temp -> processRules2(MappingRule.TEMP_TO_HUMIDITY, temp) }
            .flatten()
        println("humidityList: $humidityList")

        val locationList = humidityList
            .map { humidity -> processRules2(MappingRule.HUMIDITY_TO_LOCATION, humidity) }
            .flatten()
        println("locationList: $locationList")
        println("size: ${locationList.size}")
        return locationList
    }

    private fun processRules2(mappingRule: MappingRule, entryValueSlice: ValuesSlice): List<ValuesSlice> {

        val mappingRangesListSorted: List<MappingRuleRange> =
            mappingRulesWithRanges.getOrElse(mappingRule, { mutableListOf() })

        val remainingSlice = RemainingSlice(entryValueSlice.sliceStart, entryValueSlice.sliceRange)

        mappingRangesListSorted.forEach { mappingRuleRange -> processSliceRange2(remainingSlice, mappingRuleRange) }

        if (!remainingSlice.isClosed) {
            remainingSlice.convertRemainingToLastSlice()
        }

        return remainingSlice.outputSlices

    }

    private fun processSliceRange2(remainingSlice: RemainingSlice, mappingRuleRange: MappingRuleRange) {
        if (!remainingSlice.isClosed) {
            // SLICE UNCOVERED BEFORE FIRST RANGE
            if (isBeforeFirstRange2(remainingSlice, mappingRuleRange)) {
                sliceBeforeFirstRange2(remainingSlice, mappingRuleRange)
            }

            // SLICE COVERED BY RANGE
            if (isCoveredByRange2(remainingSlice, mappingRuleRange)) {
                sliceCoveredByRange2(remainingSlice, mappingRuleRange)
            }
        }
    }

    private fun isBeforeFirstRange2(remainingSlice: RemainingSlice, mappingRuleRange: MappingRuleRange): Boolean {
        return remainingSlice.startRemaining < mappingRuleRange.srcRangeStart
    }

    private fun isCoveredByRange2(remainingSlice: RemainingSlice, mappingRuleRange: MappingRuleRange): Boolean {
        return remainingSlice.startRemaining >= mappingRuleRange.srcRangeStart
                && remainingSlice.startRemaining <= mappingRuleRange.endRange()
    }

    private fun sliceBeforeFirstRange2(remainingSlice: RemainingSlice, mappingRuleRange: MappingRuleRange) {
        // ENTIRELY UNCOVERED : THE SLICE FINISH BEFORE THE RANGE STARTS
        if (remainingSlice.originalEnd < mappingRuleRange.srcRangeStart) {
            //CREATE SLICE
            remainingSlice.addSlice(ValuesSlice(remainingSlice.startRemaining, remainingSlice.rangeRemaining))

            //UPDATE REMAINING / NORMALLY CLOSE SHOULD NOT BE USED
            remainingSlice.updateRemaining(remainingSlice.originalEnd + 1, 0)
            remainingSlice.close()
        } else { // PARTIALLY UNCOVERED // PARTIALLY COVERED
            // CREATE UNCOVERED SLICE
            val uncoveredSeedSliceStart = remainingSlice.startRemaining
            val uncoveredSeedSliceRange = mappingRuleRange.srcRangeStart - remainingSlice.startRemaining
            val uncoveredValuesSlice = ValuesSlice(uncoveredSeedSliceStart, uncoveredSeedSliceRange)
            remainingSlice.addSlice(uncoveredValuesSlice)

            //UPDATE REMAINING
//            remainingSlice.startRemaining = mappingRuleRange.srcRangeStart
            val newRemainingStart = mappingRuleRange.srcRangeStart
            val newRemainingRange = remainingSlice.originalEnd - newRemainingStart + 1
            remainingSlice.updateRemaining(newRemainingStart, newRemainingRange)
        }
    }

    private fun sliceCoveredByRange2(remainingSlice: RemainingSlice, mappingRuleRange: MappingRuleRange) {
        // ENTIRELY COVERED
        if (remainingSlice.originalEnd <= mappingRuleRange.endRange()) {
            val coveredSliceStartSrc = remainingSlice.startRemaining
            val coveredSliceRangeSrc = remainingSlice.rangeRemaining
            val coveredSliceSrc = ValuesSlice(coveredSliceStartSrc, coveredSliceRangeSrc)
            val coveredSliceDest: ValuesSlice = convertSliceSrcToSliceDest2(coveredSliceSrc, mappingRuleRange)
            remainingSlice.addSlice(coveredSliceDest)

            remainingSlice.updateRemaining(remainingSlice.originalEnd + 1, 0)
            remainingSlice.close()

        //PARTIALLY COVERED : REMAINING
        } else {
            val coveredSliceStartSrc = remainingSlice.startRemaining
            val coveredSliceRangeSrc = mappingRuleRange.endRange() - coveredSliceStartSrc + 1
            val coveredSliceSrc = ValuesSlice(coveredSliceStartSrc, coveredSliceRangeSrc)
            val coveredSliceDest: ValuesSlice = convertSliceSrcToSliceDest2(coveredSliceSrc, mappingRuleRange)
            remainingSlice.addSlice(coveredSliceDest)

            val newStart: Long = mappingRuleRange.endRange() + 1
            remainingSlice.updateRemaining(newStart, remainingSlice.originalEnd - newStart + 1)
        }
    }

    private fun convertSliceSrcToSliceDest2(sliceSrc: ValuesSlice, range: MappingRuleRange): ValuesSlice {
        val gapFromStart: Long = sliceSrc.sliceStart - range.srcRangeStart
        val startDest: Long = range.destRangeStart + gapFromStart
        val rangeDest: Long = sliceSrc.sliceRange
        return ValuesSlice(startDest, rangeDest)
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

    private data class MappingRuleRange(
        val destRangeStart: Long, val srcRangeStart: Long, val rangeLength: Long
    ) : Comparable<MappingRuleRange> {

        fun endRange(): Long {
            return srcRangeStart + rangeLength - 1
        }

        override fun compareTo(other: MappingRuleRange): Int {
            if (this.srcRangeStart > other.srcRangeStart) {
                return 1
            } else if (this.srcRangeStart < other.srcRangeStart) {
                return -1
            } else {
                return 0
            }
        }
    }

    private data class ValuesSlice(val sliceStart: Long, val sliceRange: Long)

    private class RemainingSlice(var startRemaining: Long, var rangeRemaining: Long) {
        val outputSlices: MutableList<ValuesSlice> = mutableListOf()
        val originalEnd: Long = startRemaining + rangeRemaining - 1
        var isClosed = false

        fun addSlice(slice: ValuesSlice) {
            outputSlices.add(slice)
        }

        // Allow to convert remaining/not covered seed into a last slice
        fun convertRemainingToLastSlice() {
            outputSlices.add(ValuesSlice(startRemaining, rangeRemaining))
        }

        fun updateRemaining(startRemain: Long, rangeRemain: Long) {
            this.startRemaining = startRemain
            this.rangeRemaining = rangeRemain
        }

        fun close() {
            isClosed = true
        }
    }

}
