package com.github.hummel.saamm.lab4

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import java.awt.Color

fun researchCorrelation(statisticsArrayArray: Array<Array<Statistics>>) {
	val graphs = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.toDoubleArray().apply { sort() }
	}

	val correlation = PearsonsCorrelation()

	val coef1 = correlation.correlation(graphs[0], graphs[1])
	val coef2 = correlation.correlation(graphs[0], graphs[2])
	val coef3 = correlation.correlation(graphs[0], graphs[3])
	val coef4 = correlation.correlation(graphs[0], graphs[4])

	println("Коэффициент корреляции 50% vs 40%: $coef1")
	println("Коэффициент корреляции 50% vs 30%: $coef2")
	println("Коэффициент корреляции 50% vs 20%: $coef3")
	println("Коэффициент корреляции 50% vs 10%: $coef4")

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Индекс"
	chart.yAxisTitle = "Значение"

	chart.addSeries("50%", graphs[0].indices.map { it.toDouble() }.toDoubleArray(), graphs[0]).apply {
		markerColor = Color.GREEN
		lineColor = Color.GREEN
	}
	chart.addSeries("40%", graphs[1].indices.map { it.toDouble() }.toDoubleArray(), graphs[1]).apply {
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}
	chart.addSeries("30%", graphs[2].indices.map { it.toDouble() }.toDoubleArray(), graphs[2]).apply {
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}
	chart.addSeries("20%", graphs[3].indices.map { it.toDouble() }.toDoubleArray(), graphs[3]).apply {
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}
	chart.addSeries("10%", graphs[4].indices.map { it.toDouble() }.toDoubleArray(), graphs[4]).apply {
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}

	val regression = approximate(graphs[0], graphs[1], graphs[2], graphs[3], graphs[4])

	chart.addSeries(
		"Регрессия",
		regression.indices.map { it.toDouble() }.toDoubleArray(),
		regression
	).apply {
		markerColor = Color.RED
		lineColor = Color.RED
	}

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task4", BitmapFormat.JPG)
}

private fun approximate(vararg arrays: DoubleArray): DoubleArray {
	val n = arrays[0].size
	val m = arrays.size

	val sums = DoubleArray(n) { 0.0 }
	for (i in 0 until n) {
		for (j in 0 until m) {
			sums[i] += arrays[j][i]
		}
	}

	val averages = sums.map { it / m }

	return averages.toDoubleArray()
}