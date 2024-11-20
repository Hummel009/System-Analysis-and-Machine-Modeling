package com.github.hummel.saamm.lab4

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart

fun twoFactorExperiment() {
	val generatorChances = doubleArrayOf(0.3, 0.5, 0.7, 0.9)
	val exitTimes = doubleArrayOf(30000.0, 50000.0, 70000.0, 90000.0)

	val responses = mutableListOf<Pair<Pair<Double, Double>, Double>>()

	for (chance in generatorChances) {
		for (time in exitTimes) {
			val stats = simulateRuns(10, generatorChance = chance, exitTime = time)
			val averageResponse = stats.map { it.getProduceTime() }.average()
			responses.add(Pair(chance, time) to averageResponse)
		}
	}

	plotResponseSurface(responses)
}

private fun plotResponseSurface(responses: List<Pair<Pair<Double, Double>, Double>>) {
	val chart = XYChart(800, 600)
	chart.title = "Поверхность отклика"
	chart.xAxisTitle = "Фактор 1"
	chart.yAxisTitle = "Фактор 2"

	responses.forEach { (factors, response) ->
		chart.addSeries("Отклик", doubleArrayOf(factors.first), doubleArrayOf(factors.second), doubleArrayOf(response))
	}

	BitmapEncoder.saveBitmap(chart, "./output/response_surface", BitmapFormat.JPG)
}