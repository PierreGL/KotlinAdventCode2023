import java.io.File

object Day12 {
    fun process(filepath: String) {
        println("day12 : $filepath")
//        process1(filepath)
        process2(filepath)
    }

    private fun process1(filepath: String) {
        val records = File(filepath).readLines().map { line -> extractRecordFromLine(line) }

        //val records2 = records.subList(0, 1) // CHECK 1 2 4 / line 996 idx 995
        val countMatchingArrangement1 = records
            .map { record -> nbValidArrangement(record) }
            .sum()
        println("process1: $countMatchingArrangement1")
    }

    private fun process2(filepath: String) {
        val records = File(filepath).readLines().map { line -> extractRecordFromLine(line) }
        val unfoldRecords = records.map { record -> unfoldRecord2(record, 2) }

        val recordsToTreat = records.subList(0, 1) // CHECK 1 2 4 / line 996 idx 995
        println("unfoldRecordsSub = $recordsToTreat")

//        val sumAllValidV1 =
//            recordsToTreat.map { unfoldRecord -> ArrangementMatchingProcessor(unfoldRecord).process().count() }.sum()

        val sumAllValidV2 =
            recordsToTreat.map { unfoldRecord -> ArrangementMatchingProcessor2(unfoldRecord).process().count() }.sum()

//        println("validArrangementList= ${validArrangementList.size}")
//        validArrangementList.forEach { validArrangement -> println(validArrangement) }
//
//        val countMatchingArrangement2 = unfoldRecordsSub
//            .map { record -> nbValidArrangement(record) }
//            .sum()
        println("process2: $sumAllValidV2")
    }

    private fun unfoldRecord2(inputRecord: Record, recordMultiplier: Int): Record {
        val unfoldValueBuilder = StringBuilder(inputRecord.value)

        val initialSequenceValues: List<Int> = inputRecord.sequenceCtrl.sequenceValues

//        println("inputRecord= $inputRecord")
//        println("initialSequenceValues= $initialSequenceValues")
        val unfoldSequenceValues: MutableList<Int> = initialSequenceValues.toMutableList()

        for (idx in 2..recordMultiplier) {
            unfoldValueBuilder.append("?")
            unfoldValueBuilder.append(inputRecord.value)
            unfoldSequenceValues.addAll(initialSequenceValues)
        }
        val unfoldValue = unfoldValueBuilder.toString()
        val unfoldSequenceCtrl = Sequence(unfoldSequenceValues.toList())

        return Record(unfoldValue, unfoldSequenceCtrl)
    }

    private fun extractRecordFromLine(line: String): Record {
        val lineList = line.split(" ")

        val recordValue = lineList.get(0)
//        val hypotheticalGroups: List<String> = recordValue.split(".")

        val realSequenceContinuousGroupStr = lineList.get(1)
        val realSequenceContinuousGroupList = realSequenceContinuousGroupStr.split(",")
        val correctSequenceList: List<Int> = realSequenceContinuousGroupList.map { str -> str.toInt() }

        return Record(recordValue, Sequence(correctSequenceList))
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

        combination.listPositions.forEach { position ->
            stringBuilder.setCharAt(position, SpringType.DAMAGED.symbol)
        }

        // first version with
        val combinationAsDamagedValue = stringBuilder.toString()

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

    private fun areGroupsAndSequenceSameSize(continuousGroups: List<ContinuousGroup>, sequence: Sequence): Boolean {
        return continuousGroups.size == sequence.sequenceValues.size
    }

    private fun isSameSequence(group: ContinuousGroup, groupIdx: Int, sequence: Sequence): Boolean {
        return group.value.length == sequence.sequenceValues[groupIdx]
    }

    private data class Record(val value: String, val sequenceCtrl: Sequence) {
        val unknownSpringPositions: List<Int>

        init {
            unknownSpringPositions = extractUnknownPositionsSpring()
        }

        private fun extractUnknownPositionsSpring(): List<Int> {
            val positions = mutableListOf<Int>()
            var position = value.indexOf(SpringType.UNKNOWN.symbol)
            while (position != -1) {
                positions.add(position)
                position = value.indexOf(SpringType.UNKNOWN.symbol, position + 1)
            }
//            println(positions)
            return positions
        }
    }

    //TODO check if the sorted list solve the problem of comparison
    private data class CombinationPosition(val listPositions: List<Int>) {
        val sortedList: List<Int>
        init {
            sortedList = listPositions.sorted()
        }

        fun removeOneByIdx(idxToRemove: Int): CombinationPosition {
            val updatedList = listPositions.filterIndexed { currentIdx: Int, pos: Int -> currentIdx != idxToRemove }
            return CombinationPosition(updatedList)
        }

        //Here th contains of Set does not work as expected
        override fun equals(other: Any?): Boolean {
            return this.sortedList == other?:sortedList
        }

        override fun hashCode(): Int {
            return sortedList.hashCode()
        }

        override fun toString(): String {
            return "listPositions=$listPositions sortedList=$sortedList"
        }
    }

    private data class Sequence(val sequenceValues: List<Int>)

    private data class ContinuousGroup(val value: String)

    private data class Arrangement(
        val originalValue: String,
        val combinationAsDamagedValue: String,
        val sequenceCtrl: Sequence,
        val combinationSource: CombinationPosition
    ) {
        val continuousGroups: List<ContinuousGroup>

        init {
            val remainingUnknownAsOperationalValue: String =
                combinationAsDamagedValue.replace(SpringType.UNKNOWN.symbol, SpringType.OPERATIONAL.symbol)

            val listGroupString: List<String> = remainingUnknownAsOperationalValue.split(".")
            continuousGroups = listGroupString
                .filter { str -> str.isNotBlank() }
                .map { str -> ContinuousGroup(str) }
        }

        override fun toString(): String {
            return "originalValue=$originalValue - combinationAsDamagedValue=$combinationAsDamagedValue continuousGroups= $continuousGroups sequenceCtrl=$sequenceCtrl"
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
            //TODO check is possible
            val lastEmptyCombination = CombinationPosition(listOf())

            val allCombination: List<CombinationPosition> =
                listOf(firstFullCombination) + childCombinations + lastEmptyCombination

            return allCombination
        }

        private fun produceChildCombinations(combination: CombinationPosition): List<CombinationPosition> {
            val childCombinations: MutableList<CombinationPosition> = combination.listPositions
                .mapIndexed { idxToRemove: Int, pos: Int -> combination.removeOneByIdx(idxToRemove) }
                .filter { newChildCombination -> newChildCombination.listPositions.isNotEmpty() }
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

            val childArrangements: MutableList<Arrangement> = combination.listPositions
                .mapIndexed { idxToRemove: Int, pos: Int -> combination.removeOneByIdx(idxToRemove) }
                .filter { childCombination -> childCombination.listPositions.isNotEmpty() }
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

    // TODO go reverse direction (map reduce ?) : from small combi to large combi / identify the impossible case to avoid treating all branches
    // TODO HYPO progress by level : each level produce combination made from previous combi and combi unit : eliminate valid combi and impossible combi each level
    private data class ArrangementMatchingProcessor2(val record: Record) {
        val validArrangements: MutableList<Arrangement> = mutableListOf()
        val initialList: List<Int>
        init {
            initialList = record.unknownSpringPositions
        }

        fun process(): List<Arrangement> {
            val initialUnitCombination = initialList
                .map { pos -> CombinationPosition(listOf(pos)) }
                .filter { combinationPosition -> unfilterAndStoreValidArrangement(combinationPosition) }

            nextLevel(initialUnitCombination)
            println("validArrangements = $validArrangements")
            return validArrangements
        }

        fun nextLevel(combinationToEvaluate: List<CombinationPosition>) {
            println("combinationToEvaluate = $combinationToEvaluate")

            val allNextLevelCombination: List<CombinationPosition> =
                generateAllNextLevel(combinationToEvaluate)
            //TODO identify impossible combi ?  And remove them from the filtered list

            val nextLevelCombiNotValid: List<CombinationPosition> = allNextLevelCombination
                .filter { combi -> unfilterAndStoreValidArrangement(combi)}

            // TODO check if we need more out condition for that recursive method
            if (nextLevelCombiNotValid.isNotEmpty() && hasNotReachLastLevel(nextLevelCombiNotValid)) {
                nextLevel(nextLevelCombiNotValid)
            }

        }

        private fun unfilterAndStoreValidArrangement(combination: CombinationPosition): Boolean {
            val arrangement = produceArrangementFromCombination(combination, record)
            if (isValidArrangement(arrangement)) {
                validArrangements.add(arrangement)
                //Here the case an arrangement is valid there is no way than the branch with additional position could be valid
                return false
            }
            return true
        }

        private fun generateAllNextLevel(combinations: List<CombinationPosition>): List<CombinationPosition> {

            val alreadyExistingCombi: MutableSet<CombinationPosition> = mutableSetOf()

            // We filter here the redundant ex: 1-2 + 3 = 1-2-3 AND 2-3 + 1 = 1-2-3
            // TODO look if the equals of list is good or if there is need to sort or use another list
            val nextLevelFiltered = combinations
                .map { combinationCurrentLevel ->
                    generateNextLevelsCombi(combinationCurrentLevel)
                }
                .flatten()
                .filter { combinationPosition ->
                    if (alreadyExistingCombi.contains(combinationPosition)) {
                        false
                    }
                    alreadyExistingCombi.add(combinationPosition)
                    true
                }

            return nextLevelFiltered
        }

        private fun generateNextLevelsCombi(combination: CombinationPosition): List<CombinationPosition> {
            return initialList.map { pos -> generateCombination(combination, pos) }.filterNotNull()
        }

        private fun generateCombination(combination: CombinationPosition, pos: Int): CombinationPosition? {
            // Here we filter the case 1-2 + 2 : which would be redundant with the combi input 1-2
            if (!combination.listPositions.contains(pos)) {
                val newListPositions: List<Int> = combination.listPositions + pos
                return CombinationPosition(newListPositions)
            }
            return null
        }

        private fun hasNotReachLastLevel(nextLevelCombiNotValid: List<CombinationPosition>): Boolean {
            return nextLevelCombiNotValid.first().listPositions.size != initialList.size
        }


    }


}
