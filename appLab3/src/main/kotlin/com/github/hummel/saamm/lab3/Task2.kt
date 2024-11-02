package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import java.awt.Color
import kotlin.math.sqrt

fun researchConfidenceInterval(statisticsArray: Array<Statistics>) {
	val produceTimeList = statisticsArray.map { it.getProduceTime() }.toDoubleArray()

	val intervals = mutableListOf<Pair<Double, Double>>()

	val description = DescriptiveStatistics(produceTimeList)

	val n = description.n
	val mean = description.mean
	val stdDev = description.standardDeviation

	val tDist = TDistribution(n - 1.0)
	val alpha = 0.05
	val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

	val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

	val (from, to) = mean - marginOfError to mean + marginOfError

	intervals.addAll(Array(statisticsArray.size) { from to to })

	val chart = XYChart(1600, 900)
	chart.title = "Доверительные интервалы и средние значения"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Значения"

	chart.addSeries(
		"Доверительный интервал L",
		statisticsArray.indices.map { it + 1.0 }.toDoubleArray(),
		intervals.map { it.first }.toDoubleArray()
	).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}

	chart.addSeries(
		"Доверительный интервал U",
		statisticsArray.indices.map { it + 1.0 }.toDoubleArray(),
		intervals.map { it.second }.toDoubleArray()
	).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}

	chart.addSeries("Средние значения", statisticsArray.indices.map { it + 1.0 }.toDoubleArray(), produceTimeList)
		.apply {
			xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
			markerColor = Color.RED
			lineColor = Color.RED
		}

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task2", BitmapFormat.JPG)
}