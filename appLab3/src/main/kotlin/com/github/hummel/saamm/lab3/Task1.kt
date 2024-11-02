package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.CategoryChart
import kotlin.math.ceil
import kotlin.math.log2

fun researchDistributionGraph(statisticsArray: Array<Statistics>) {
	val produceTimeList = statisticsArray.map { it.getProduceTime() }

	val numberOfIntervals = ceil(log2(produceTimeList.size.toDouble())).toInt() + 1

	val minValue = produceTimeList.minOrNull() ?: 0.0
	val maxValue = produceTimeList.maxOrNull() ?: 1.0
	val intervalSize = (maxValue - minValue) / numberOfIntervals

	val histogram = IntArray(numberOfIntervals)
	produceTimeList.forEach { time ->
		val index = ((time - minValue) / intervalSize).toInt().coerceIn(0, numberOfIntervals - 1)
		histogram[index]++
	}

	val chart = CategoryChart(1600, 900)
	chart.title = "Гистограмма времени изготовления деталей"
	chart.xAxisTitle = "Время (с)"
	chart.yAxisTitle = "Встречаемость"

	val xData = DoubleArray(numberOfIntervals) { minValue + it * intervalSize + intervalSize / 2 }
	val yData = histogram.map { it.toDouble() }.toDoubleArray()

	chart.addSeries("Гистограмма", xData, yData)

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task1", BitmapFormat.JPG)
}

fun researchDistributionIdea(statisticsArray: Array<Statistics>) {
	val produceTimeList = statisticsArray.map { it.getProduceTime() }.toDoubleArray()

	val description = DescriptiveStatistics(produceTimeList)

	val mean = description.mean
	val stdDev = description.standardDeviation

	val normalDistribution = NormalDistribution(mean, stdDev)

	val ksTest = KolmogorovSmirnovTest()
	val pValue = ksTest.kolmogorovSmirnovTest(normalDistribution, produceTimeList)

	val alpha = 0.05

	val isNormal = pValue > alpha

	println("Данные${if (isNormal) "" else " не"} нормально распределены.")
}