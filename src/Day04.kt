import java.io.File

class Day04(val filePath: String) {

    val mapCardNumber: MutableMap<Int, Int> = mutableMapOf()

    fun process() {
        println("$filePath")
        val result = File(filePath).readLines()
            .mapIndexed { idx, line -> extractLine(idx, line) }
            .map(OutputLine::result)
            .sum()
        println("$filePath = $result")
    }

    fun process2() {
        println("$filePath")
        File(filePath).readLines().forEachIndexed { idx, line -> processCard2(idx + 1, line) }

        val result2 = mapCardNumber.values.sum()


//            .map(OutputLine::result)
//            .sum()
        println("$filePath = $result2")
    }


    private fun extractLine(cardNb: Int, card: String): OutputLine {
        val cardList = card.split(":")
        val numbersStr = cardList.get(1)
        val numbersList = numbersStr.split("|")
        val winningNbStr = numbersList.get(0)
        val nbYouHaveStr = numbersList.get(1)

        println("win: $winningNbStr youHave: $nbYouHaveStr")

        val winningNbList = winningNbStr.trim().split(" ").filter { str -> str.isNotBlank() }
            .map { nbStr -> nbStr.trim().toInt() }
        val nbYouHaveList = nbYouHaveStr.trim().split(" ").filter { str -> str.isNotBlank() }
            .map { nbStr -> nbStr.trim().toInt() }

        val matchedNbList = nbYouHaveList.filter { nbYouHave -> winningNbList.contains(nbYouHave) }

        return OutputLine(winningNbList, nbYouHaveList, matchedNbList, computeLineResult(matchedNbList))
    }

    private fun computeLineResult(matchedNbList: List<Int>): Long {
        val size = matchedNbList.size

        if (size == 1) {
            return 1
        } else {
            return Math.pow(2.toDouble(), size.toDouble() - 1).toLong()
        }
    }


    private fun processCard2(cardNb: Int, card: String) {
        upsertCard(cardNb, 1)

        val cardList = card.split(":")
        val numbersStr = cardList.get(1)
        val numbersList = numbersStr.split("|")
        val winningNbStr = numbersList.get(0)
        val nbYouHaveStr = numbersList.get(1)

        println("win: $winningNbStr youHave: $nbYouHaveStr")

        val winningNbList = winningNbStr.trim().split(" ").filter { str -> str.isNotBlank() }
            .map { nbStr -> nbStr.trim().toInt() }
        val nbYouHaveList = nbYouHaveStr.trim().split(" ").filter { str -> str.isNotBlank() }
            .map { nbStr -> nbStr.trim().toInt() }

        val matchedNbList = nbYouHaveList.filter { nbYouHave -> winningNbList.contains(nbYouHave) }

        val newMatchedNumbers = matchedNbList.size

        println("cardNb: $cardNb : newMatchedNumber: $newMatchedNumbers")

        upsertNextCards(cardNb, newMatchedNumbers)
    }

    private fun upsertCard(cardNb: Int, addedValue: Int) {
        // UPDATE
        if (mapCardNumber.contains(cardNb)) {
            val existingValue = mapCardNumber.getValue(cardNb)
            mapCardNumber[cardNb] = existingValue + addedValue
            // CREATE
        } else {
            mapCardNumber[cardNb] = addedValue
        }
    }

    private fun upsertNextCards(currentCardNb: Int, matchedNumber: Int) {
        val currentCardValue = mapCardNumber[currentCardNb] ?: 1

        for (idx in currentCardNb + 1..currentCardNb + matchedNumber) {
            upsertCard(idx, currentCardValue)
        }

        println("map : $mapCardNumber")
    }
}

private data class OutputLine(
    val winningNumbers: List<Int>, val numbersYouHave: List<Int>,
    val matchedNb: List<Int>, val result: Long
)
