import java.io.File

object Day12 {
    fun process(filepath: String) {
        println("day12 : $filepath")
//        process1(filepath)
        process2(filepath)
    }

    private fun process1(filepath: String) {
        val records = File(filepath).readLines().map { line -> extractRecordFromLine(line, null) }

        //val records2 = records.subList(0, 1) // CHECK 1 2 4 / line 996 idx 995
        val countMatchingArrangement1 = records
            .map { record -> nbValidArrangement(record) }
            .sum()
        println("process1: $countMatchingArrangement1")
    }

    private fun process2(filepath: String) {
        val records = File(filepath).readLines().map { line -> extractRecordFromLine(line, null) }
        val unfoldRecords = records.map { record -> unfoldRecord2(record, 5, 10) }

        val recordsToTreat = unfoldRecords.subList(0, 6) // CHECK 1 2 4 / line 996 idx 995

//        val sumAllValidV2 =
//            recordsToTreat.map { unfoldRecord -> ArrangementMatchingCounterProcessor(unfoldRecord).process() }.sum()

        val recordResultList: List<RecordResult> =
            recordsToTreat.map { unfoldRecord -> RecordSubBlockProcessor(unfoldRecord).processRecordResult() }

        recordResultList.forEach { recordResult ->
            println("recordResult= $recordResult")
            recordResult.subBlockResults.forEach { subBlockResult ->
                println("subBlockResult= $subBlockResult")

            }

        }


//        println("process2: $sumAllValidV2")
    }

    private fun unfoldRecord2(inputRecord: Record, recordMultiplier: Int, sizeBlock: Int): Record {
        val unfoldValueBuilder = StringBuilder(inputRecord.value)

        val initialSequenceValues: List<Int> = inputRecord.sequenceCtrl.sequenceValues

        val unfoldSequenceValues: MutableList<Int> = initialSequenceValues.toMutableList()

        for (idx in 2..recordMultiplier) {
            unfoldValueBuilder.append("?")
            unfoldValueBuilder.append(inputRecord.value)
            unfoldSequenceValues.addAll(initialSequenceValues)
        }
        val unfoldValue = unfoldValueBuilder.toString()
        val unfoldSequenceCtrl = Sequence(unfoldSequenceValues.toList())

        return Record(unfoldValue, unfoldSequenceCtrl, sizeBlock)
    }

    private fun extractRecordFromLine(line: String, sizeblock: Int?): Record {
        val lineList = line.split(" ")

        val recordValue = lineList.get(0)
//        val hypotheticalGroups: List<String> = recordValue.split(".")

        val realSequenceContinuousGroupStr = lineList.get(1)
        val realSequenceContinuousGroupList = realSequenceContinuousGroupStr.split(",")
        val correctSequenceList: List<Int> = realSequenceContinuousGroupList.map { str -> str.toInt() }

        return Record(recordValue, Sequence(correctSequenceList), sizeblock)
    }

    private fun nbValidArrangement(record: Record): Int {
        val allValidArrangement: List<Arrangement> = produceAllArrangements(record)
            .filter { arrangement -> isValidArrangement(arrangement) }

        val numberValidArrangement = allValidArrangement.size
        return numberValidArrangement
    }

    private fun produceAllArrangements(record: Record): List<Arrangement> {

        val allCombination = CombinationProcessor(record.unknownSpringPositions).process()

        val arrangementList: List<Arrangement> = allCombination.map { combinationPosition ->
            produceArrangementFromCombination(combinationPosition, record)
        }

        return arrangementList

    }

    private fun produceArrangementFromCombination(combination: CombinationPosition, record: Record): Arrangement {

        val originalValue = record.value
        val stringBuilder = StringBuilder(originalValue)

        combination.listPositionsValues.forEach { position ->
            stringBuilder.setCharAt(position, SpringType.DAMAGED.symbol)
        }

        // first version with
        val combinationAsDamagedValue: String = stringBuilder.toString()

        return Arrangement(originalValue, combinationAsDamagedValue, record.sequenceCtrl, combination)
    }

    private fun isValidArrangement(arrangement: Arrangement): Boolean {
        val sequence: Sequence = arrangement.sequenceCtrl
        val continuousGroups: List<ContinuousGroup> = arrangement.continuousGroups


//        println("arrangementMatchSequence arrangement=$arrangement ")

        var allSameSequence = false
        if (areGroupsAndSequenceSameSize(continuousGroups, sequence)) {
            allSameSequence = continuousGroups
                .mapIndexed { groupIdx: Int, group: ContinuousGroup -> isSameSequence(group, groupIdx, sequence) }
                .all { bool: Boolean -> bool }
        }

        return allSameSequence
    }

    /**
     * An arrangement is impossible if the subSequence is not valid AND cannot become valid with additional sequence
     * element.
     * Ex : seqControl : 1,3,1
     * The arrangement 2, 1 is impossible. Return true.
     * The arrangement 1, 3 is not valid but not impossible. Return false.
     * */
    private fun isImpossible(arrangement: Arrangement): Boolean {

        val subSequence: List<Int> = arrangement.subSequence
        val ctrlSequenceValue: List<Int> = arrangement.sequenceCtrl.sequenceValues

        //RULE of size not correct : adding new change can reduce the number of group so the size of the subsequence
        //RULE of subSequence element higher of the ctrlSeq element is not correct : adding new change can move the position of the higher


//        // If the subSequence is bigger it is impossible
//        if (subSequence.size > ctrlSequence.size) {
//            return true
//        } else {
//            val ctrlSequenceToCompare = ctrlSequence.subList(0, subSequence.size)
//            subSequence.mapIndexed { subSequenceIdx, subSequenceValue ->
//                val ctrlSequenceValue = ctrlSequenceToCompare[subSequenceIdx]
//                // If one of the subSequence value exceed the ctrl sequence value at the same position.
//                // It makes that combination and all its child impossible
//                if (subSequenceValue > ctrlSequenceValue) {
//                    return true
//                }
//            }
//
//            return ctrlSequenceToCompare != subSequence
//        }


        // RULE of higher certainly correct but not very efficient to eliminate possibilities
        val higherImpossibleValue =
            subSequence.any { subSequuenceValue -> subSequuenceValue > arrangement.sequenceCtrl.higherValue }


        return higherImpossibleValue
    }

    private fun areGroupsAndSequenceSameSize(continuousGroups: List<ContinuousGroup>, sequence: Sequence): Boolean {
        return continuousGroups.size == sequence.sequenceValues.size
    }

    private fun isSameSequence(group: ContinuousGroup, groupIdx: Int, sequence: Sequence): Boolean {
        return group.value.length == sequence.sequenceValues[groupIdx]
    }

    private fun extractUnknownSpringPositions(value: String): List<Int> {
        val positions = mutableListOf<Int>()
        var position = value.indexOf(SpringType.UNKNOWN.symbol)
        while (position != -1) {
            positions.add(position)
            position = value.indexOf(SpringType.UNKNOWN.symbol, position + 1)
        }
        return positions
    }

    private data class Record(val value: String, val sequenceCtrl: Sequence, val sizeBlock: Int?) {
        val unknownSpringPositions: List<Int> = extractUnknownSpringPositions(value)
        val subBlocks: List<SubBlock> = generateSubBlocks()

        //TODO initialize the list of subBlocks

        private fun generateSubBlocks(): List<SubBlock> {
            if (sizeBlock != null) {
                return value.chunked(sizeBlock).map { blockValue -> SubBlock(blockValue) }
            }
            return listOf()
        }

        override fun toString(): String {
            return "RECORD value=$value - sequenceCtrl=$sequenceCtrl subBlocks=$subBlocks"
        }
    }

    private data class SubBlock(val value: String) {
        val unknownSpringPositions: List<Int> = extractUnknownSpringPositions(value)
    }

    private data class CombinationPosition(val listPositionsValues: List<Int>) {
        //Represent the index in the initial list of last position of the combination
        var lastPosIndexInitialList: Int = 0

        fun removeOneByIdx(idxToRemove: Int): CombinationPosition {
            val updatedList =
                listPositionsValues.filterIndexed { currentIdx: Int, pos: Int -> currentIdx != idxToRemove }
            return CombinationPosition(updatedList)
        }

        override fun toString(): String {
            return "list=${listPositionsValues} - lastPosIndexInitialList=$lastPosIndexInitialList"
        }
    }

    private data class Sequence(val sequenceValues: List<Int>) {
        val higherValue: Int = sequenceValues.max()
    }

    private data class ContinuousGroup(val value: String)

    private data class Arrangement(
        val originalValue: String,
        val combinationAsDamagedValue: String,
        val sequenceCtrl: Sequence,
        val combinationSource: CombinationPosition
    ) {
        val continuousGroups: List<ContinuousGroup>
        val subSequence: List<Int>


        init {
            val remainingUnknownAsOperationalValue: String =
                combinationAsDamagedValue.replace(SpringType.UNKNOWN.symbol, SpringType.OPERATIONAL.symbol)

            val listGroupString: List<String> = remainingUnknownAsOperationalValue.split(".")
            continuousGroups = listGroupString
                .filter { str -> str.isNotBlank() }
                .map { str -> ContinuousGroup(str) }

            subSequence = continuousGroups.map { continuousGroup -> continuousGroup.value.length }
        }

        override fun toString(): String {
            return "combinationAsDamagedValue=$combinationAsDamagedValue - " +
                    "combinationSource=$combinationSource " +
                    "subSequence=$subSequence"
        }
    }

    private enum class SpringType(val symbol: Char) {
        OPERATIONAL('.'),
        DAMAGED('#'),
        UNKNOWN('?');
    }

    private data class CombinationProcessor(val initialList: List<Int>) {

        //        val mapCombination: MutableMap<String, CombinationPosition> = mutableMapOf()
        val combinationSet: MutableSet<CombinationPosition> = mutableSetOf()

        fun process(): List<CombinationPosition> {
            val firstFullCombination = CombinationPosition(initialList)
            val childCombinations: List<CombinationPosition> = produceChildCombinations(firstFullCombination)
            val lastEmptyCombination = CombinationPosition(listOf())

            val allCombination: List<CombinationPosition> =
                listOf(firstFullCombination) + childCombinations + lastEmptyCombination

            return allCombination
        }

        private fun produceChildCombinations(combination: CombinationPosition): List<CombinationPosition> {
            val childCombinations: MutableList<CombinationPosition> = combination.listPositionsValues
                .mapIndexed { idxToRemove: Int, pos: Int -> combination.removeOneByIdx(idxToRemove) }
                .filter { newChildCombination -> newChildCombination.listPositionsValues.isNotEmpty() }
                .filter { newChildCombination -> !combinationSet.contains(newChildCombination) }
                .map { newChildCombination ->
                    combinationSet.add(newChildCombination)
                    newChildCombination
                }
                .toCollection(mutableListOf())

            val nextChildCombinations: List<CombinationPosition> = childCombinations
                .map { childCombination -> produceChildCombinations(childCombination) }
                .flatten()

            return childCombinations + nextChildCombinations
        }

    }

    private data class ArrangementMatchingProcessor(val record: Record) {

        //        val mapCombination: MutableMap<String, CombinationPosition> = mutableMapOf()
        val alreadyTreatedCombinationSet: MutableSet<CombinationPosition> = mutableSetOf()

        fun process(): List<Arrangement> {
            val initialList = record.unknownSpringPositions
            val firstFullCombination = CombinationPosition(initialList)
            val firstArrangement = produceArrangementFromCombination(firstFullCombination, record)
            val childArrangements: List<Arrangement> = produceValidChildArrangements(firstArrangement)
            val lastEmptyCombination = CombinationPosition(listOf());
            val lastArrangement: Arrangement = produceArrangementFromCombination(lastEmptyCombination, record)

            val allArrangements: MutableList<Arrangement> = mutableListOf()
            if (isValidArrangement(firstArrangement)) {
                allArrangements.add(firstArrangement)
            }
            allArrangements.addAll(childArrangements)
            if (isValidArrangement(lastArrangement)) {
                allArrangements.add(lastArrangement)
            }

            println("---")
            println("Result processing: $record")
            allArrangements.forEach { validArrangement ->
                println(validArrangement)
            }

            return allArrangements
        }

        private fun produceValidChildArrangements(arrangement: Arrangement): List<Arrangement> {
            val combination = arrangement.combinationSource

            val childArrangements: MutableList<Arrangement> = combination.listPositionsValues
                .mapIndexed { idxToRemove: Int, pos: Int -> combination.removeOneByIdx(idxToRemove) }
                .filter { childCombination -> childCombination.listPositionsValues.isNotEmpty() }
                .filter { childCombinationNotEmpty -> !alreadyTreatedCombinationSet.contains(childCombinationNotEmpty) }
                .map { newChildCombination ->
                    alreadyTreatedCombinationSet.add(newChildCombination)
                    produceArrangementFromCombination(newChildCombination, record)
                }
                .toCollection(mutableListOf())
//            println("childArrangement= $childArrangements")

            val childValidArrangement: MutableList<Arrangement> = childArrangements
                .filter { newArrangement -> isValidArrangement(newArrangement) }
                .toCollection(mutableListOf())
//            println("childValidArrangement= $childValidArrangement")

            val nextValidChildArrangements: List<Arrangement> = childArrangements
                .map { childArrang -> produceValidChildArrangements(childArrang) }
                .flatten()

            return childValidArrangement + nextValidChildArrangements
        }

    }

    private data class ArrangementMatchingCounterProcessor(val record: Record) {
        //val validArrangements: MutableList<Arrangement> = mutableListOf()
        val initialList: List<Int>
        var counterValidArrangements: Long = 0

        init {
            initialList = record.unknownSpringPositions
        }

        fun process(): Long {
            println("recordToTreat = $record")

            val initialUnitCombination = initialList
                .mapIndexed { positionIndex, positionValue ->
                    val unitCombi = CombinationPosition(listOf(positionValue))
                    unitCombi.lastPosIndexInitialList = positionIndex
                    unitCombi
                }
                .filter { combinationPosition -> unfilterAndCountValidArrangement(combinationPosition) }

            nextLevel(initialUnitCombination)
            println("counterValidArrangements = $counterValidArrangements")
            //println("validArrangements = $validArrangements")
            return counterValidArrangements
        }

        fun nextLevel(combinationToEvaluate: List<CombinationPosition>) {
            val allNextLevelCombination: List<CombinationPosition> =
                generateNextLevelsAllCombi(combinationToEvaluate)

            val nextLevelCombiNotValid: List<CombinationPosition> = allNextLevelCombination
                .filter { combi -> unfilterAndCountValidArrangement(combi) }

            if (nextLevelCombiNotValid.isNotEmpty() && hasNotReachLastLevel(nextLevelCombiNotValid)) {
                nextLevel(nextLevelCombiNotValid)
            }

        }

        private fun unfilterAndCountValidArrangement(combination: CombinationPosition): Boolean {
            val arrangement: Arrangement = produceArrangementFromCombination(combination, record)
//            println("check arrangement $arrangement")
            if (isValidArrangement(arrangement)) {
                println("valid $arrangement")
                //validArrangements.add(arrangement)
                counterValidArrangements++
                //Here the case an arrangement is valid there is no way than the branch with additional position could be valid
                return false
            } else if (isImpossible(arrangement)) {
                println("Impossible $arrangement")
                return false
            }
            return true
        }

        private fun generateNextLevelsAllCombi(combinations: List<CombinationPosition>): List<CombinationPosition> {

            val nextLevelFiltered = combinations
                .map { combinationCurrentLevel -> generateNextLevelsOneCombi(combinationCurrentLevel) }
                .flatten()

            return nextLevelFiltered
        }

        private fun generateNextLevelsOneCombi(combination: CombinationPosition): List<CombinationPosition> {

            val newCombinationList: MutableList<CombinationPosition> = mutableListOf()
            // We iterate only from the last position to the end of the initial list
            //Ex : combi 0-2 : we iterate from 3 to create 0-2-3, 0-2-4... and avoid recreate 0-1-2
            for (positionIndex: Int in combination.lastPosIndexInitialList + 1..initialList.size - 1) {
                val positionValue = initialList.get(positionIndex)
                newCombinationList.add(generateCombination(combination, positionValue, positionIndex))
            }

            return newCombinationList
        }

        private fun generateCombination(
            combination: CombinationPosition,
            positionValue: Int,
            posIndexInInitialList: Int
        ): CombinationPosition {
            val newListPositions: List<Int> = combination.listPositionsValues + positionValue
            val newCombi = CombinationPosition(newListPositions)
            newCombi.lastPosIndexInitialList = posIndexInInitialList
            return newCombi
        }

        private fun hasNotReachLastLevel(nextLevelCombiNotValid: List<CombinationPosition>): Boolean {
            return nextLevelCombiNotValid.first().listPositionsValues.size != initialList.size
        }
    }

    /**
     * For each subBlock of each record produces the list of sequences possible.
     * Each sequence has a list of int and a mergableBefore and a mergableAfter info
     * Produce a recordResult or List<SubBlockResult> ?
     * */
    private data class RecordSubBlockProcessor(val record: Record) {
        fun processRecordResult(): RecordResult {
            val subBlockResults: List<SubBlockResult> = record.subBlocks.mapIndexed { blockIdx, subBlock ->
                generateSubBlockResult(blockIdx, subBlock)
            }
            return RecordResult(subBlockResults)
        }

        fun generateSubBlockResult(blockIdx: Int, subBlock: SubBlock): SubBlockResult {

            val subSequenceList: MutableList<SubSequence> = mutableListOf()
            val subBlockValue = subBlock.value

            val initialUnitCombination: List<CombinationPosition> = subBlock.unknownSpringPositions
                .mapIndexed { positionIndex, positionValue ->
                    val unitCombi = CombinationPosition(listOf(positionValue))
                    unitCombi.lastPosIndexInitialList = positionIndex
                    unitCombi
                }


            val subSequencesLevelOne: List<SubSequence> =
                generateSubSequencesFromCombinationList(initialUnitCombination, subBlockValue)

            subSequenceList.addAll(subSequencesLevelOne)

            nextLevel(initialUnitCombination, subBlock, subSequenceList)


            return SubBlockResult((blockIdx + 1).toString(), subSequenceList)
        }

        fun generateSubSequencesFromCombinationList(
            combinations: List<CombinationPosition>,
            subBlockValue: String
        ): List<SubSequence> {
            return combinations.map { combination -> generateSubSequenceFromCombination(combination, subBlockValue) }
        }

        fun generateSubSequenceFromCombination(combination: CombinationPosition, blockValue: String): SubSequence {
            val originalValue = blockValue
            val stringBuilder = StringBuilder(originalValue)

            combination.listPositionsValues.forEach { position ->
                stringBuilder.setCharAt(position, SpringType.DAMAGED.symbol)
            }

            val combinationAsDamagedValue: String = stringBuilder.toString()

            val remainingUnknownAsOperationalValue: String =
                combinationAsDamagedValue.replace(SpringType.UNKNOWN.symbol, SpringType.OPERATIONAL.symbol)

            val mergableBefore: Boolean = remainingUnknownAsOperationalValue.first() == SpringType.DAMAGED.symbol
            val mergableAfter: Boolean = remainingUnknownAsOperationalValue.last() == SpringType.DAMAGED.symbol


            val listGroupString: List<String> = remainingUnknownAsOperationalValue.split(".")
            val continuousGroups: List<ContinuousGroup> = listGroupString
                .filter { str -> str.isNotBlank() }
                .map { str -> ContinuousGroup(str) }

            val subSequenceValues: List<Int> = continuousGroups.map { continuousGroup -> continuousGroup.value.length }

            return SubSequence(subSequenceValues, mergableBefore, mergableAfter)
        }

        fun nextLevel(
            combinationToEvaluate: List<CombinationPosition>,
            subBlock: SubBlock,
            subSequences: MutableList<SubSequence>
        ) {
            val allNextLevelCombination: List<CombinationPosition> =
                generateNextLevelsAllCombi(combinationToEvaluate, subBlock)

            val subSequencesThisLevel = generateSubSequencesFromCombinationList(allNextLevelCombination, subBlock.value)

            subSequences.addAll(subSequencesThisLevel)

            // TODO check out condition
            if (allNextLevelCombination.isNotEmpty() && hasNotReachLastLevel(allNextLevelCombination, subBlock)) {
                nextLevel(allNextLevelCombination, subBlock, subSequences)
            }
        }

        private fun generateNextLevelsAllCombi(
            combinations: List<CombinationPosition>,
            subBlock: SubBlock
        ): List<CombinationPosition> {

            val nextLevelFiltered = combinations
                .map { combinationCurrentLevel -> generateNextLevelsOneCombi(combinationCurrentLevel, subBlock) }
                .flatten()

            return nextLevelFiltered
        }

        private fun generateNextLevelsOneCombi(
            combination: CombinationPosition,
            subBlock: SubBlock
        ): List<CombinationPosition> {
            val initialList = subBlock.unknownSpringPositions
            val newCombinationList: MutableList<CombinationPosition> = mutableListOf()
            // We iterate only from the last position to the end of the initial list
            //Ex : combi 0-2 : we iterate from 3 to create 0-2-3, 0-2-4... and avoid recreate 0-1-2
            for (positionIndex: Int in combination.lastPosIndexInitialList + 1..initialList.size - 1) {
                val positionValue = initialList.get(positionIndex)
                newCombinationList.add(generateCombination(combination, positionValue, positionIndex))
            }

            return newCombinationList
        }

        private fun generateCombination(
            combination: CombinationPosition,
            positionValue: Int,
            posIndexInInitialList: Int
        ): CombinationPosition {
            val newListPositions: List<Int> = combination.listPositionsValues + positionValue
            val newCombi = CombinationPosition(newListPositions)
            newCombi.lastPosIndexInitialList = posIndexInInitialList
            return newCombi
        }

        private fun hasNotReachLastLevel(
            nextLevelCombiNotValid: List<CombinationPosition>,
            subBlock: SubBlock
        ): Boolean {
            return nextLevelCombiNotValid.first().listPositionsValues.size != subBlock.unknownSpringPositions.size
        }
    }

    //TODO implement
    /**
     * For each RecordResult. Evaluate possible combination of sequence for each block.
     * At each block retain only eligible sequence which match the begining of the sequence control.
     * Consider the particular case of mergable after it can match even the last element of the subsequence is smaller
     * Ex:
     * 1, 3, 1 is eligible (can be concatenate)
     * 1,3,1,2 is eligible : still compatible to 1,3,1,6 (last element can be merged and obtain 6)
     * But 1,3,1,7 is not (last too high)
     * And 2,3,1.. is not (one element missmatch)
     * */
    private class BlockSequenceAggregatorProcessor {

    }

    private data class RecordResult(val subBlockResults: List<SubBlockResult>)

    private data class SubBlockResult(val blockName: String, val subSequence: List<SubSequence>) {
        override fun toString(): String {
            return "blockName=$blockName - subSeqNumber=${subSequence.size} - subSequence=$subSequence"
        }
    }

    private data class SubSequence(
        val sequenceValues: List<Int>,
        val mergableBefore: Boolean,
        val mergableAfter: Boolean
    )

}
