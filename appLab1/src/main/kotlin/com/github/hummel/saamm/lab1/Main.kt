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

fun generateRandomNumbers(x0: Long, a: Long, c: Long, m: Long, count: Int): List<Float> {
	val randomNumbers = mutableListOf<Float>()
	var xn = x0

	repeat(count) {
		xn = (a * xn + c) % m
		randomNumbers.add(xn.toFloat() / m)
	}

	return randomNumbers
}

fun buildHistogram(numbers: List<Float>, bins: Int): IntArray {
	val histogram = IntArray(bins) { 0 }
	val binSize = 1.0 / bins

	numbers.asSequence().map {
		floor(it / binSize).toInt().coerceAtMost(bins - 1)
	}.forEach {
		histogram[it]++
	}

	return histogram
}

fun chiSquareTest(histogram: IntArray, total: Int): Float {
	val expected = total.toFloat() / histogram.size

	val chiSquare = histogram.sumOf {
		(it - expected).pow(2).toDouble() / expected
	}.toFloat()

	return chiSquare
}

fun kolmogorovSmirnovTest(numbers: List<Float>): Pair<Float, Float> {
	val sortedNumbers = numbers.sorted()
	val n = numbers.size
	var kpMax = 0.0f
	var kmMax = 0.0f

	for (i in sortedNumbers.indices) {
		val empiricalF = i.toFloat() / n
		val theoreticalF = sortedNumbers[i]
		kpMax = maxOf(kpMax, theoreticalF - empiricalF)
		kmMax = maxOf(kmMax, empiricalF - theoreticalF)
	}

	val kP = sqrt(n.toFloat()) * kpMax
	val kM = sqrt(n.toFloat()) * kmMax

	return kP to kM
}

fun plotKSGraph(numbers: List<Float>) {
	val sortedNumbers = numbers.sorted()
	val n = numbers.size

	val empiricalY = mutableListOf<Float>()
	val theoreticalY = mutableListOf<Float>()

	for (i in sortedNumbers.indices) {
		val empiricalF = i.toFloat() / n
		val theoreticalF = sortedNumbers[i]

		empiricalY.add(empiricalF)
		theoreticalY.add(theoreticalF)
	}

	val chart = XYChart(750, 600)
	chart.title = "KS Test"
	chart.xAxisTitle = "x"
	chart.yAxisTitle = "F(x)"

	val commonX = (0..99).map { it / 100.0f }

	chart.addSeries("Empir function", commonX.map { it.toDouble() }, empiricalY.map { it.toDouble() })

	chart.addSeries("Theor function", commonX.map { it.toDouble() }, theoreticalY.map { it.toDouble() })

	SwingWrapper(chart).displayChart().isVisible = true
}

operator fun String.times(count: Int): String = this.repeat(count)