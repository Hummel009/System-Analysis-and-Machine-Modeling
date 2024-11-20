package com.github.hummel.saamm.lab3

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
	val correlationCoefficients = DoubleArray(graphs.size - 1)

	for (i in 0 until graphs.size - 1) {
		correlationCoefficients[i] = correlation.correlation(graphs.last(), graphs[i])
		println("Коэффициент корреляции: ${correlationCoefficients[i]}")
	}

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Индекс"
	chart.yAxisTitle = "Значение"

	for (i in graphs.indices) {
		val seriesName = "${i * 10 + 10}%"
		chart.addSeries(seriesName, graphs[i].indices.map { it.toDouble() }.toDoubleArray(), graphs[i]).apply {
			markerColor = if (i == graphs.lastIndex) Color.GREEN else Color.BLUE
			lineColor = if (i == graphs.lastIndex) Color.GREEN else Color.BLUE
		}
	}

	val regression = approximate(*graphs.toTypedArray())
	chart.addSeries(
		"Регрессия", regression.indices.map { it.toDouble() }.toDoubleArray(), regression
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