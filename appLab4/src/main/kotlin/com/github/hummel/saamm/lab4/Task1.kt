package com.github.hummel.saamm.lab4

import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

fun researchCorrelation(statisticsArrayArray: Array<Array<Statistics>>) {
	val graphs = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.toDoubleArray().apply { sort() }
	}

	val correlation = PearsonsCorrelation()
	val correlationCoefficients = DoubleArray(graphs.size - 1)
	val errors = DoubleArray(graphs.size - 1)

	for (i in 0 until graphs.size - 1) {
		correlationCoefficients[i] = correlation.correlation(graphs.last(), graphs[i])
		errors[i] = calculateError(graphs[i], graphs.last())
	}

	val best = determineBestApproximations(correlationCoefficients, errors)
	println(best)

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Индекс"
	chart.yAxisTitle = "Значение"

	for (i in graphs.indices) {
		val seriesName = "${(i + 1) * 12.5}%"
		chart.addSeries(seriesName, graphs[i].indices.map { it.toDouble() }.toDoubleArray(), graphs[i]).apply {
			markerColor = if (i == graphs.lastIndex) Color.GREEN else Color.BLUE
			lineColor = if (i == graphs.lastIndex) Color.GREEN else Color.BLUE
		}
	}

	val regressionLinear = linearApproximation(*graphs.toTypedArray())
	chart.addSeries(
		"Линейная регрессия", regressionLinear.indices.map { it.toDouble() }.toDoubleArray(), regressionLinear
	).apply {
		markerColor = Color.RED
		lineColor = Color.RED
	}

	val regressionNonLinear = polynomialApproximation(*graphs.toTypedArray())
	chart.addSeries(
		"Нелинейная регрессия", regressionNonLinear.indices.map { it.toDouble() }.toDoubleArray(), regressionNonLinear
	).apply {
		markerColor = Color.ORANGE
		lineColor = Color.ORANGE
	}

	BitmapEncoder.saveBitmap(chart, "./output/task1", BitmapFormat.JPG)
}

private fun calculateError(data: DoubleArray, reference: DoubleArray): Double =
	sqrt(data.zip(reference).map { (d, r) -> (d - r).pow(2) }.average())

private fun determineBestApproximations(correlationCoefficients: DoubleArray, errors: DoubleArray): String {
	require(correlationCoefficients.size == errors.size) { "Массивы должны иметь одинаковую длину" }

	var bestCorrelationIndex = -1
	var bestErrorIndex = -1
	var bestCorrelationScore = Double.NEGATIVE_INFINITY
	var bestErrorScore = Double.POSITIVE_INFINITY

	for (i in correlationCoefficients.indices) {
		if (correlationCoefficients[i] > bestCorrelationScore) {
			bestCorrelationScore = correlationCoefficients[i]
			bestCorrelationIndex = i
		}

		if (errors[i] < bestErrorScore) {
			bestErrorScore = errors[i]
			bestErrorIndex = i
		}
	}

	return buildString {
		append("Лучший по корреляции: График ${bestCorrelationIndex + 1} с коэффициентом корреляции ${correlationCoefficients[bestCorrelationIndex]}.\n")
		append("Лучший по погрешности: График ${bestErrorIndex + 1} с ошибкой ${errors[bestErrorIndex]}.")
	}
}

private fun linearApproximation(vararg arrays: DoubleArray): DoubleArray {
	val n = arrays[0].size
	val m = arrays.size

	val sums = DoubleArray(n) { 0.0 }
	for (i in 0 until n) {
		for (j in 0 until m) {
			sums[i] += arrays[j][i]
		}
	}

	return sums.map { it / m }.toDoubleArray()
}

private fun polynomialApproximation(vararg arrays: DoubleArray): DoubleArray {
	val n = arrays[0].size
	val m = arrays.size

	val approximatedValues = DoubleArray(n)
	val obs = WeightedObservedPoints()

	for (j in 0 until m) {
		for (i in 0 until n) {
			obs.add(i.toDouble(), arrays[j][i])
		}
	}

	val fitter = PolynomialCurveFitter.create(2)

	val coefficients = fitter.fit(obs.toList())

	for (i in 0 until n) {
		approximatedValues[i] = evaluatePolynomial(coefficients, i.toDouble())
	}

	return approximatedValues
}

private fun evaluatePolynomial(coefficients: DoubleArray, x: Double): Double {
	var result = 0.0
	for (i in coefficients.indices) {
		result += coefficients[i] * x.pow(i.toDouble())
	}
	return result
}