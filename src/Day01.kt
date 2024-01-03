import java.io.File

class Day01(val filePath: String) {
    val digits: String = "0123456789"
    val digitsAndLetter = listOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
    );

    val mapDigit = mapOf(
        "0" to 0,
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,
        "zero" to 0,
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9
    )

    fun process2() {
        val totalResult = File(filePath).readLines().map { line -> lineResult(line) }.sum();
        println("$filePath : $totalResult")
    }

    private fun lineResult(line: String): Int {
        //println(line)
        val firstDigit = findFirst(line)
        val lastDigit = findLast(line)

        // val lastDigit = line.last { c -> isDigit(c) }
        val lineRes = "$firstDigit$lastDigit"
        println(lineRes)
        //println(lineRes)


        // val idx: Int = line.indexOfAny(digitsLetter)
        return lineRes.toInt();
    }

//    private fun findFirstDigit(line: String): Int {
//        return line.first { c: Char -> isDigit(c) }.digitToInt()
//    }

    private fun findFirst(line: String): Int? {
        val pair = line.findAnyOf(digitsAndLetter)
        println("first: $pair")
        return mapDigit.get(pair?.second)
    }

    private fun findLast(line: String): Int? {
        val pair = line.findLastAnyOf(digitsAndLetter)
        println("last: $pair")
        return mapDigit.get(pair?.second)
    }
}
