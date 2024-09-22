package com.github.hummel.saamm.lab2

import kotlin.math.floor
import kotlin.math.pow

fun main() {
	val x0 = 123456789L
	val a = 152353L
	val c = 5.0.pow(11).toLong()
	val m = 2.0.pow(30).toLong()

	val quantity = 100
	val randomNumbers = generateRandomNumbers(x0, a, c, m, quantity)
	val histogram = buildHistogram(randomNumbers, 20)

	println("Histogram:")
	histogram.forEachIndexed { index, value ->
		println("${index + 1}: ${"*" * value}")
	}

	val chiSquareResult = chiSquareTest(histogram, quantity)
	println("Chi Square: $chiSquareResult")

	val ksResult = kolmogorovSmirnovTest(randomNumbers)
	println("KS+: ${ksResult.first}")
	println("KS-: ${ksResult.second}")
}

fun generateRandomNumbers(x0: Long, a: Long, c: Long, m: Long, count: Int): List<Double> {
	val randomNumbers = mutableListOf<Double>()
	var xn = x0

	repeat(count) {
		xn = (a * xn + c) % m
		randomNumbers.add(xn.toDouble() / m)
	}

	return randomNumbers
}

fun buildHistogram(numbers: List<Double>, bins: Int): IntArray {
	val histogram = IntArray(bins) { 0 }
	val binSize = 1.0 / bins

	numbers.asSequence().map {
		floor(it / binSize).toInt().coerceAtMost(bins - 1)
	}.forEach {
		histogram[it]++
	}

	return histogram
}

fun chiSquareTest(histogram: IntArray, total: Int): Double {
	val expected = total.toDouble() / histogram.size

	val chiSquare = histogram.sumOf {
		(it - expected).pow(2) / expected
	}

	return chiSquare
}

fun kolmogorovSmirnovTest(numbers: List<Double>): Pair<Double, Double> {
	val sortedNumbers = numbers.sorted()
	val n = numbers.size
	var dPlus = 0.0
	var dMinus = 0.0

	for (i in sortedNumbers.indices) {
		val empiricalCdf = (i + 1).toDouble() / n
		val theoreticalCdf = sortedNumbers[i]
		dPlus = maxOf(dPlus, empiricalCdf - theoreticalCdf)
		dMinus = maxOf(dMinus, theoreticalCdf - (i.toDouble() / n))
	}

	return dPlus to dMinus
}

operator fun String.times(count: Int): String = this.repeat(count)