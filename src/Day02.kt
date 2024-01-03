import java.io.File

class Day02(val filePath: String) {

    private val limitByColor = mapOf("red" to 12, "green" to 13, "blue" to 14)

    fun process1() {
        val totalResult = File(filePath).readLines().map { line -> lineResult1(line) }.sum();
        println("$filePath : $totalResult")
    }

    private fun lineResult1(line: String): Int {
        val list = line.split(":")
        val gameName = list.get(0)
        val gameNameValue = gameName.split(" ").get(1).toInt()

        val cubesList = list.get(1)
        val cubesSets = cubesList.split(";")

        val isValidGame = cubesSets.all { cubeSet -> isValidCubeSet(cubeSet) }

        return if (isValidGame) gameNameValue else 0
    }

    private fun isValidCubeSet(cubeSet: String): Boolean {
        println(cubeSet)
        val cubByColorSet = cubeSet.split(",")
        println(cubByColorSet)
        return cubByColorSet.all { cubColor -> isValidCubColor(cubColor) }
    }

    private fun isValidCubColor(cubColor: String): Boolean {
        val cubeColorPair = cubColor.trim().split(" ")
        val cubColorNb = cubeColorPair.get(0).toInt()
        val color = cubeColorPair.get(1)

        val limit = limitByColor.get(color) ?: -1
        return cubColorNb <= limit
    }

    fun process2() {
        val result = File(filePath).readLines().map { line -> Game(line).gameResult() }.sum()
        println("$filePath : $result")

        //println("$filePath : $totalResult")
    }

    class Game(gameString: String) {
        private var maxRed: Int;
        private var maxBlue: Int;
        private var maxGreen: Int;

        private val cubeSetList: List<CubeSet>

        init {
            val allCubesSetsAsString = gameString.split(":").get(1)
            val cubesSetAsList = allCubesSetsAsString.split(";")
            cubeSetList = cubesSetAsList.map { cubeSet -> CubeSet(cubeSet) }

            maxRed = cubeSetList.map { cubeSet -> cubeSet.redValue() }.max()
            maxGreen = cubeSetList.map { cubeSet -> cubeSet.greenValue() }.max()
            maxBlue = cubeSetList.map { cubeSet -> cubeSet.blueValue() }.max()
        }

        fun gameResult(): Int {
            return maxRed * maxBlue * maxGreen
        }
    }

    class CubeSet(cubeSetStr: String) {
        val cubNbList: List<CubeNb> = cubeSetStr.split(",").map(::CubeNb)

        fun redValue(): Int {
            return cubNbList.filter { cubeNb -> cubeNb.color == "red" }.map { cubeNb -> cubeNb.nb }.sum()
        }

        fun greenValue(): Int {
            return cubNbList.filter { cubeNb -> cubeNb.color == "green" }.map { cubeNb -> cubeNb.nb }.sum()
        }

        fun blueValue(): Int {
            return cubNbList.filter { cubeNb -> cubeNb.color == "blue" }.map { cubeNb -> cubeNb.nb }.sum()
        }

    }

    class CubeNb(cubNbStr: String) {
        val nb: Int
        val color: String

        init {
            val cubNbAsList = cubNbStr.trim().split(" ")
            nb = cubNbAsList.get(0).toInt()
            color = cubNbAsList.get(1)
        }
    }
}
