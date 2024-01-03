import java.io.File

class Day09 {

    fun process(filePath: String) {
        println("$filePath") // 114

        val sequenceHistoryList: List<SequenceHistory> = File(filePath).readLines().map { line ->
            val listValues: List<Long> = convertToList(line)
            SequenceHistory(listValues)
        }

        val result1: Long = sequenceHistoryList.map { sequenceHistory -> sequenceHistory.nextValue }.sum()
        println("result1: $result1")
        val result2: Long = sequenceHistoryList.map { sequenceHistory -> sequenceHistory.backValue }.sum()
        println("result2: $result2")
    }

    private fun convertToList(strValue: String): List<Long> {
        return strValue.split(" ").map { str -> str.toLong() }
    }

    private class SequenceHistory(val listValues: List<Long>) {
        val childSequenceHistory: SequenceHistory?
        val initialSize: Int
        val nextValue: Long
        val backValue: Long


        init {
            initialSize = listValues.size
            if (listValues.any() { value -> value != 0L }) {
                childSequenceHistory = childSequenceHistory()
            } else {
                childSequenceHistory = null
            }
            nextValue = generateNextValue()
            backValue = generateBackValue()
        }

        private fun childSequenceHistory(): SequenceHistory {

            val childListValue: MutableList<Long> = mutableListOf()
            // Iteration until the last but one
            for (idx: Int in 0..listValues.size - 2) {
                val gap: Long = listValues.get(idx + 1) - listValues.get(idx)
                childListValue.add(gap)
            }

            return SequenceHistory(childListValue)
        }

        fun generateNextValue(): Long {
            val childNextValue: Long? = childSequenceHistory?.nextValue
            val lastValue: Long = listValues.get(initialSize - 1)
            val nextValue: Long = (childNextValue?:0) + lastValue
            return nextValue
        }

        fun generateBackValue(): Long {
            val childBackValue: Long? = childSequenceHistory?.backValue
            val firstValue: Long = listValues.get(0)
            val backValue: Long = firstValue - (childBackValue?:0)
            return backValue
        }

    }
}
