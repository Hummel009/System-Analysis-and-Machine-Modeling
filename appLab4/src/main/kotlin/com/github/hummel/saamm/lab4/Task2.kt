package com.github.hummel.saamm.lab4

import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import java.awt.Color
import kotlin.math.sqrt

fun researchComparison(statisticsArrayArray: Array<Array<Statistics>>) {
	val yData = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.average()
	}.toDoubleArray()

	val lowerBounds = mutableListOf<Double>()
	val upperBounds = mutableListOf<Double>()

	statisticsArrayArray.forEach { statsArray ->
		val sampleStats = DescriptiveStatistics()
		statsArray.forEach { stat -> sampleStats.addValue(stat.getProduceTime()) }

		val mean = sampleStats.mean
		val stdDev = sampleStats.standardDeviation
		val n = sampleStats.n

		val tDist = TDistribution(n - 1.0)
		val alpha = 0.05
		val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

		val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

		lowerBounds.add(mean - marginOfError)
		upperBounds.add(mean + marginOfError)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Параметры: время работы генератора, время работы станков"
	chart.yAxisTitle = "Отклик: время производства изделия"

	val xData = DoubleArray(yData.size) { it + 1.0 }

	chart.addSeries("Оригинальные данные", xData, yData).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
		markerColor = Color.RED
		lineColor = Color.RED
	}

	chart.addSeries("Нижняя граница доверительного интервала", xData, lowerBounds.toDoubleArray()).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}
	chart.addSeries("Верхняя граница доверительного интервала", xData, upperBounds.toDoubleArray()).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task2", BitmapFormat.JPG)
}