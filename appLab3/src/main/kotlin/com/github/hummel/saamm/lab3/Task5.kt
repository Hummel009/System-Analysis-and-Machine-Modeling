package com.github.hummel.saamm.lab3

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import java.awt.Color

fun main() {
	val statisticsArrayOrig = generateSetsOfSimulationsForce(0.0f, 0.5f)
	val timeSeries = statisticsArrayOrig[0].productTimes.keys.map { it.toDouble() }
	val valueSeries = statisticsArrayOrig[0].productTimes.values.map { it.toDouble() }

	val chart = XYChart(1200, 675)
	chart.title = "Изменение отклика в модельном времени"
	chart.xAxisTitle = "Время"
	chart.yAxisTitle = "Значение"

	chart.addSeries("Значение", timeSeries, valueSeries).setXYSeriesRenderStyle(
		XYSeries.XYSeriesRenderStyle.Line
	).setMarkerColor(
		Color.GRAY
	).setLineColor(
		Color.GRAY
	).fillColor = Color.GRAY

	SwingWrapper(chart).displayChart()

	val transitionIndex =
		findTransitionPeriod(statisticsArrayOrig[0].productTimes.values.map { it.toDouble() }.toDoubleArray())
	println(
		"Переходный период находится на: ${
			statisticsArrayOrig[0].productTimes.keys.map { it.toDouble() / 5 }.toDoubleArray()[transitionIndex]
		}"
	)
}

fun findTransitionPeriod(data: DoubleArray): Int {
	val n = data.size
	val stats = DescriptiveStatistics()

	var transitionPoint = -1
	var maxFValue = Double.NEGATIVE_INFINITY

	for (i in 1 until n) {
		val firstPart = data.copyOfRange(0, i)
		val secondPart = data.copyOfRange(i, n)

		stats.clear()
		for (value in firstPart) stats.addValue(value)
		val variance1 = stats.variance

		stats.clear()
		for (value in secondPart) stats.addValue(value)
		val variance2 = stats.variance

		val fValue = variance1 / variance2
		if (fValue > maxFValue) {
			maxFValue = fValue
			transitionPoint = i
		}
	}

	return transitionPoint
}