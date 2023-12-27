import java.io.File

class Day3(val filePath: String) {

    val symbolsList: String = "#*+$"

    fun process() {
        println("$filePath")
        val outputLines: List<OutputLine> = File(filePath)
            .readLines()
            .mapIndexed { idx, value -> extractOutputLine(idx, value) }
            .toList()

        val allNumberSeq: List<NumberSeq> = outputLines.flatMap { o -> o.numberSeqList }
        val allSymbols: List<Symbol> = outputLines.flatMap { o -> o.symbols }

        val result1 = allNumberSeq
            .filter { numberSeq -> isCloseToSymbol(numberSeq, allSymbols) }
            .map { numberSeq -> numberSeq.value.toInt() }
            .sum()

        val result2 = allSymbols
            .map { symbol -> extractSymbolComputation(symbol, allNumberSeq) }
            .filterNotNull()
            .sum()

        println("day3 result1=$result1")
        println("day3 result2=$result2")

    }

    private fun extractSymbolComputation(symbol: Symbol, allNumberSeq: List<NumberSeq>): Int? {
        val matchSeqNb = allNumberSeq.filter { numberSeq -> isClose(numberSeq, symbol) }
        var result: Int? = null
        if (matchSeqNb.size >= 2) {
            result = matchSeqNb
                .map { numberSeq -> numberSeq.value.toInt() }
                .reduce{acc, value -> acc * value}
        }

        return result;
    }

    private fun extractOutputLine(lineIdx: Int, line: String): OutputLine {

        val numberSeqList: MutableList<NumberSeq> = mutableListOf()
        val symbolList: MutableList<Symbol> = mutableListOf()

        var currentNumber = ""
        var currentNumberSeq = NumberSeq()


        line.toList().forEachIndexed { idxNumber, char ->
            if (char.isDigit()) {
                // IS NEW
                if (currentNumber == "") {
                    currentNumberSeq = NumberSeq()
                    currentNumberSeq.line = lineIdx
                    currentNumberSeq.startAbs = idxNumber
                }

                currentNumber += char
                currentNumberSeq.value += char
                currentNumberSeq.endAbs = idxNumber

                // IS LAST EOL
                if (idxNumber == line.length - 1) {
                    numberSeqList.add(currentNumberSeq)
                }

            } else {

                // IS SYMBOL
                if (isSymbol(char)) {
                    symbolList.add(Symbol(char, idxNumber, lineIdx))
                }

                if (currentNumber != "") {
                    numberSeqList.add(currentNumberSeq)
                    currentNumber = ""
                }
            }
        }


        return OutputLine(numberSeqList, symbolList)
    }

    private fun isSymbol(char: Char): Boolean {
        return char != '.'
    }

    private fun isCloseToSymbol(numberSeq: NumberSeq, allSymbol: List<Symbol>): Boolean {
        val foundSymbol = allSymbol.find { symbol -> isClose(numberSeq, symbol) }
        return foundSymbol != null

    }

    private fun isACloseSymbol(numberSeq: NumberSeq, allSymbol: List<Symbol>): Symbol? {
        val foundSymbol = allSymbol.find { symbol -> isClose(numberSeq, symbol) }
        return foundSymbol
    }

    private fun isClose(numberSeq: NumberSeq, symbol: Symbol): Boolean {
        return closeOrd(numberSeq, symbol) && closeAbs(numberSeq, symbol)
    }

    private fun closeOrd(numberSeq: NumberSeq, symbol: Symbol): Boolean {
        return Math.abs(numberSeq.line - symbol.ord) <= 1
    }

    private fun closeAbs(numberSeq: NumberSeq, symbol: Symbol): Boolean {
        return symbol.abs >= numberSeq.startAbs - 1 && symbol.abs <= numberSeq.endAbs + 1
    }

    private data class OutputLine(val numberSeqList: List<NumberSeq>, val symbols: List<Symbol>)

    private class NumberSeq {
        var startAbs: Int = -1
        var endAbs: Int = -1
        var line: Int = -1
        var value: String = ""

        override fun toString(): String {
            return "NumberSeq(startAbs='$startAbs', endAbs=$endAbs, line=$line, value=$value)"
        }
    }

    private data class Symbol(val value: Char, val abs: Int, val ord: Int)
}