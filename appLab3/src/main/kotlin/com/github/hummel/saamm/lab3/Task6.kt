package com.github.hummel.saamm.lab3

import kotlin.math.abs

fun main() {
	val statisticsArrayLong = generateSetsOfSimulationsForce1(0.0f, 0.5f, 20000.0f + 50 * 30000)
	val statisticsArrayShort = generateSetsOfSimulationsForce50(0.0f, 0.5f, 50000.0f)

	val avg1 = mutableListOf<Float>()
	val avg2 = mutableListOf<Float>()

	for (statistics in statisticsArrayLong) {
		val times = statistics.productTimes.filter { (key, _) -> key > 20000 }
		val chunkedTimes = times.values.chunked(times.size / statisticsArrayShort.size)
		for (chunk in chunkedTimes) {
			avg1.addAll(chunk)
		}
	}
	for (statistics in statisticsArrayShort) {
		val times = statistics.productTimes.filter { (key, _) -> key > 20000 }
		avg2.addAll(times.values)
	}

	val variance1 = calculateVariance(avg1)
	val variance2 = calculateVariance(avg2)

	println("Дисперсия первой выборки: $variance1")
	println("Дисперсия второй выборки: $variance2")

	val tStatistic = performTTest(avg1, avg2)

	val fStatistic = variance1 / variance2
	val fCritical = calculateFCriticalValue(avg1.size - 1, avg2.size - 1, 0.10)

	println("Критическое значение F: $fCritical")

	if (fStatistic > fCritical) {
		println("Дисперсии выборок значительно различаются.")
	} else {
		println("Дисперсии выборок мало различаются.")
	}

	val tCritical = calculateTCriticalValue(0.05, avg1.size + avg2.size - 2)

	println("Критическое значение t: $tCritical")

	if (abs(tStatistic) > tCritical) {
		println("Разница между выборками мало значима.")
	} else {
		println("Разница между выборками значима.")
	}

	println("${avg1.average()}")
	println("${avg2.average()}")
}