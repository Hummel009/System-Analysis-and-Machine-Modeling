package com.github.hummel.saamm.lab1

import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

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

	val (kP, kM) = kolmogorovSmirnovTest(randomNumbers)
	println("Kn+: $kP")
	println("Kn-: $kM")

	plotKSGraph(randomNumbers)
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
		(it - expected).pow(2).toDouble() / expected
	}.toDouble()

	return chiSquare
}

fun kolmogorovSmirnovTest(numbers: List<Double>): Pair<Double, Double> {
	val sortedNumbers = numbers.sorted()
	val n = numbers.size
	var kpMax = 0.0
	var kmMax = 0.0

	for (i in sortedNumbers.indices) {
		val empiricalF = i.toDouble() / n
		val theoreticalF = sortedNumbers[i]
		kpMax = maxOf(kpMax, theoreticalF - empiricalF)
		kmMax = maxOf(kmMax, empiricalF - theoreticalF)
	}

	val kP = sqrt(n.toDouble()) * kpMax
	val kM = sqrt(n.toDouble()) * kmMax

	return kP to kM
}

fun plotKSGraph(numbers: List<Double>) {
	val sortedNumbers = numbers.sorted()
	val n = numbers.size

	val empiricalY = mutableListOf<Double>()
	val theoreticalY = mutableListOf<Double>()

	for (i in sortedNumbers.indices) {
		val empiricalF = i.toDouble() / n
		val theoreticalF = sortedNumbers[i]

		empiricalY.add(empiricalF)
		theoreticalY.add(theoreticalF)
	}

	val chart = XYChart(750, 600)
	chart.title = "KS Test"
	chart.xAxisTitle = "x"
	chart.yAxisTitle = "F(x)"

	val commonX = (0..99).map { it / 100.0 }

	chart.addSeries("Empir function", commonX.map { it.toDouble() }, empiricalY.map { it.toDouble() })

	chart.addSeries("Theor function", commonX.map { it.toDouble() }, theoreticalY.map { it.toDouble() })

	SwingWrapper(chart).displayChart().isVisible = true
}

operator fun String.times(count: Int): String = this.repeat(count)