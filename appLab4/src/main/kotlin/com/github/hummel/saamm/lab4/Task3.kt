package com.github.hummel.saamm.lab4

import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChart
import kotlin.math.sqrt


fun researchAccuracy(statisticsArrayArray: Array<Array<Statistics>>) {
	val range = 1..statisticsArrayArray.lastIndex

	val margins = mutableListOf<Double>()

	for (i in range) {
		val statisticsArray = statisticsArrayArray[i]

		val produceTimeList = statisticsArray.map { it.getProduceTime() }.toDoubleArray()

		val description = DescriptiveStatistics(produceTimeList)

		val n = description.n
		val stdDev = description.standardDeviation

		val tDist = TDistribution(n - 1.0)
		val alpha = 0.05
		val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

		val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

		margins.add(marginOfError)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Зависимость погрешности от прогонов"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Дельта"

	chart.addSeries("Дельта", range.map { it + 1 }, margins)

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task3", BitmapFormat.JPG)
}