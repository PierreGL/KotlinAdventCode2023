import java.io.File
import java.lang.RuntimeException

class Day07 {

    fun process(filePath: String) {
        println("$filePath")
        //HandType.values().forEach { handType -> println(handType.ordinal) }

        val allHandsUnsorted: MutableList<Hand> = extractHands(filePath)
        val allHandsSorted = allHandsUnsorted.sorted()
        println("allHandsUnsorted: $allHandsUnsorted")
        println("allHandsSorted: $allHandsSorted")
        val result1 = allHandsSorted
            .mapIndexed { idx, hand -> HandWithRank(hand, idx + 1) }
            .map { handWithRank -> handWithRank.computeScore() }
            .sum()

        println("result1: $result1")
    }

    private fun extractHands(filePath: String): MutableList<Hand> {
        return File(filePath).readLines().map { line -> extractHand(line) }.toMutableList()
    }

    private fun extractHand(line: String): Hand {
        val lineList = line.split(" ")
        val cards = lineList.get(0).trim()
        val bid = lineList.get(1).trim().toInt()
        return Hand(cards, bid)
    }

    private data class Hand(val cardsStr: String, val bid: Int) : Comparable<Hand> {

        val cards: List<Card>
        val handType: HandType

        init {
            cards = extractCards(cardsStr)
//            handType = processType(cards)
            handType = processType2(cards)
        }

        private fun extractCards(crdStr: String): List<Card> {
            return crdStr.toCharArray().map { labelChar: Char -> Card.findFromLabel(labelChar.toString()) }
        }

        private fun processType(cardsList: List<Card>): HandType {
            val mapCardOccurence: MutableMap<Card, Int> = mutableMapOf()
            cardsList.stream().forEach { card ->
                val currentOcc = mapCardOccurence.getOrDefault(card, 0)
                val newOcc = currentOcc + 1
                mapCardOccurence.put(card, newOcc)
            }

            val cardValueList: List<CardValue> = mapCardOccurence.keys.map {
                card -> CardValue(card, mapCardOccurence.getOrDefault(card, -1))
            }

            println("cardsList: $cardsList")
            println("mapCardOccurence: $mapCardOccurence")
            println("cardValueList: $cardValueList")
            val foundType = findType(cardValueList)
            println("foundType: $foundType")
            return foundType
        }


        private fun processType2(cardsList: List<Card>): HandType {
            val mapCardOccurence: MutableMap<Card, Int> = mutableMapOf()
            cardsList.stream().forEach { card ->
                val currentOcc = mapCardOccurence.getOrDefault(card, 0)
                val newOcc = currentOcc + 1
                mapCardOccurence.put(card, newOcc)
            }

            val cardValueListBeforeJoker: List<CardValue> = mapCardOccurence.keys.map {
                    card -> CardValue(card, mapCardOccurence.getOrDefault(card, -1))
            }

            // FIND JOKER NB
            val jokerNb = mapCardOccurence.getOrDefault(Card.JOKER, 0)

            // FIND HIGHEST NOT JOKER
            val cardValueListSorted: List<CardValue> = cardValueListBeforeJoker.sortedDescending()
            val highestNotJoker: CardValue? = cardValueListSorted.firstOrNull { cardValue -> cardValue.card != Card.JOKER }

            if (highestNotJoker != null) {
                val highestNotJokerOcc = mapCardOccurence.getOrDefault(highestNotJoker.card, -1)
                val newHighestCardOcc = highestNotJokerOcc + jokerNb
                mapCardOccurence.put(highestNotJoker.card, newHighestCardOcc)
                mapCardOccurence.remove(Card.JOKER)
            }

            val cardValueListAfterJoker: List<CardValue> = mapCardOccurence.keys.map {
                    card -> CardValue(card, mapCardOccurence.getOrDefault(card, -1))
            }

            println("-----")
            println("cardsList: $cardsList")
            println("mapCardOccurence: $mapCardOccurence")
            println("cardValueListBeforeJoker: $cardValueListBeforeJoker")
            println("cardValueListSorted: $cardValueListSorted")
            println("cardValueListAfterJoker: $cardValueListAfterJoker")
            val foundType = findType(cardValueListAfterJoker)
            println("foundType: $foundType")
            return foundType
        }

        private fun findType(cardValueList: List<CardValue>): HandType {
            if (isFiveOfKind(cardValueList)) {
                return HandType.FIVE_OF_KIND
            } else if (isFourOfKind(cardValueList)) {
                return HandType.FOUR_OF_KIND
            } else if (isFullHouse(cardValueList)) {
                return HandType.FULL_HOUSE
            } else if (isThreeOfKind(cardValueList)) {
                return HandType.THREE_OF_KIND
            } else if (isTwoPair(cardValueList)) {
                return HandType.TWO_PAIR
            } else if (isOnePair(cardValueList)) {
                return HandType.ONE_PAIR
            } else {
                return HandType.HIGH_CARD
            }
        }

        private fun isFiveOfKind(cardValueList: List<CardValue>): Boolean {
            return cardValueList.any { cardValue -> cardValue.occ == 5 }
        }

        private fun isFourOfKind(cardValueList: List<CardValue>): Boolean {
            return cardValueList.any { cardValue -> cardValue.occ == 4 }
        }

        private fun isFullHouse(cardValueList: List<CardValue>): Boolean {
            return cardValueList.any { cardValue -> cardValue.occ == 3 }
                    && cardValueList.any { cardValue -> cardValue.occ == 2 }
        }

        private fun isThreeOfKind(cardValueList: List<CardValue>): Boolean {
            return cardValueList.any { cardValue -> cardValue.occ == 3 }
                    && cardValueList.none { cardValue -> cardValue.occ == 2 }
        }

        private fun isTwoPair(cardValueList: List<CardValue>): Boolean {
            return cardValueList.filter { cardValue -> cardValue.occ == 2 }.size == 2
        }

        private fun isOnePair(cardValueList: List<CardValue>): Boolean {
            return cardValueList.filter { cardValue -> cardValue.occ == 2 }.size == 1
        }

        override fun compareTo(other: Hand): Int {

            val handTypeComparison = this.handType.compareTo(other.handType)
            if (handTypeComparison != 0) {
                return handTypeComparison
                // EQUALITY OF TYPE NEED TO COMPARE CARD ONE BY ONE
            } else {
                return compareListOfCards(other)
            }

        }

        private fun compareListOfCards(other: Hand): Int {
            for (idxCard in 0..cards.size - 1) {
                val cardComparison: Int = this.cards.get(idxCard).compareTo(other.cards.get(idxCard))
                //If unequal we can go out otherwise we can continue the iteration
                if (cardComparison != 0) {
                    return cardComparison
                }
            }
            return 0
        }

        override fun toString(): String {
            return "Card[cardStr=$cardsStr, card=$cards bid=$bid, handType=$handType]"
        }
    }

    private data class CardValue(val card: Card, val occ: Int): Comparable<CardValue> {
        override fun compareTo(other: CardValue): Int {
            return this.occ.compareTo(other.occ)
        }
    }

    private data class HandWithRank(val hand: Hand, val rank: Int) {
        fun computeScore(): Int {
            return rank * hand.bid
        }
    }

    private enum class HandType {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_KIND,
        FULL_HOUSE,
        FOUR_OF_KIND,
        FIVE_OF_KIND
    }

    private enum class Card(val label: String) {
        JOKER("J"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        T("T"),
//        JOKER("J"),
        QUEEN("Q"),
        KING("K"),
        AS("A");

        companion object {
            fun findFromLabel(label: String): Card {
                val found = Card.values().find { cardLabel -> cardLabel.label == label }
                return found ?: throw RuntimeException("Impossible value")

            }
        }
    }
}
