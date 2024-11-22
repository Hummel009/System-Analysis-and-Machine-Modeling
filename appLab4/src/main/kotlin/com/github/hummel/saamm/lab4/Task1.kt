package com.github.hummel.saamm.lab4

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

fun researchCorrelation(statisticsArrayArray: Array<Array<Statistics>>) {
	val yData = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.average()
	}.toDoubleArray()

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Параметр: время генерации детали"
	chart.yAxisTitle = "Отклик: время производства изделия"

	val xData = DoubleArray(yData.size) { i -> (i + 1) * 0.05 }

	chart.addSeries("Оригинальные данные", xData, yData).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.GREEN
		lineColor = Color.GREEN
	}
	val linearFit = linearApproximation(xData, yData)
	chart.addSeries("Линейная аппроксимация", xData, linearFit).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.RED
		lineColor = Color.RED
	}
	val polynomialFit = polynomialApproximation(xData, yData)
	chart.addSeries("Полиномиальная аппроксимация", xData, polynomialFit).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.RED
		lineColor = Color.RED
	}

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

		val marginOfError = tValue * (stdDev / sqrt(n.toDouble())) + 0.5

		lowerBounds.add(mean - marginOfError)
		upperBounds.add(mean + marginOfError)
	}

	chart.addSeries("Нижняя граница доверительного интервала", xData, lowerBounds.toDoubleArray()).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}
	chart.addSeries("Верхняя граница доверительного интервала", xData, upperBounds.toDoubleArray()).apply {
		xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
		markerColor = Color.BLUE
		lineColor = Color.BLUE
	}

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task1", BitmapFormat.JPG)

	val linearRMSE = calculateRMSE(yData, linearFit)
	val polynomialRMSE = calculateRMSE(yData, polynomialFit)

	println("Среднеквадратичная ошибка линейной аппроксимации: $linearRMSE")
	println("Среднеквадратичная ошибка полиномиальной аппроксимации: $polynomialRMSE")

	if (linearRMSE < polynomialRMSE) {
		println("Линейная аппроксимация лучше.")
	} else {
		println("Полиномиальная аппроксимация лучше.")
	}
}

private fun calculateRMSE(original: DoubleArray, approximation: DoubleArray): Double {
	val sumSquaredErrors = original.indices.sumOf {
		(original[it] - approximation[it]).pow(2)
	}

	return sqrt(sumSquaredErrors / original.size)
}

private fun linearApproximation(xData: DoubleArray, yData: DoubleArray): DoubleArray {
	val n = xData.size
	val xMean = xData.average()
	val yMean = yData.average()

	var numerator = 0.0
	var denominator = 0.0

	for (i in 0 until n) {
		numerator += (xData[i] - xMean) * (yData[i] - yMean)
		denominator += (xData[i] - xMean).pow(2)
	}

	val a = numerator / denominator
	val b = yMean - a * xMean

	return DoubleArray(n) { a * xData[it] + b }
}

private fun polynomialApproximation(xData: DoubleArray, yData: DoubleArray): DoubleArray {
	val fitter = PolynomialCurveFitter.create(2)
	val points = WeightedObservedPoints()

	for (i in xData.indices) {
		points.add(xData[i], yData[i])
	}

	val coefficients = fitter.fit(points.toList())

	val polynomialFunction = PolynomialFunction(coefficients)

	return DoubleArray(xData.size) { polynomialFunction.value(xData[it]) }
}