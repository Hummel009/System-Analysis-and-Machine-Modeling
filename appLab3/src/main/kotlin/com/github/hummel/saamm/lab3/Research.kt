package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.CategoryChart
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import java.awt.Color
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.sqrt

fun researchAverageStats(stats: Array<Statistics>) {
	val quantity = stats.size

	val partsType1 = stats.sumOf { it.partsType1 }.toDouble() / quantity
	val partsType2 = stats.sumOf { it.partsType2 }.toDouble() / quantity
	val accumulatorPartsType1 = stats.sumOf { it.accumulatorPartsType1 }.toDouble() / quantity
	val accumulatorPartsType2 = stats.sumOf { it.accumulatorPartsType2 }.toDouble() / quantity
	val packPlaceProducts = stats.sumOf { it.packPlaceProducts }.toDouble() / quantity
	val packPlacePackets = stats.sumOf { it.packPlacePackets }.toDouble() / quantity
	val storagePackets = stats.sumOf { it.storagePackets }.toDouble() / quantity
	val duration = stats.sumOf { it.duration } / quantity
	val produceTime = stats.sumOf { it.getProduceTime() } / quantity

	val redColor = "\u001B[31m"
	val resetColor = "\u001B[0m"

	println(
		"""
		${redColor}Средняя статистика по $quantity заводам:
		Создано деталей A: $partsType1,
		Создано деталей B: $partsType2,
		Обработано деталей A: $accumulatorPartsType1,
		Обработано деталей B: $accumulatorPartsType2,
		Собрано изделий: $packPlaceProducts,
		Собрано партий: $packPlacePackets,
		Партий на складе: $storagePackets,
		Общее время (с): $duration,
		Время на производство одного изделия (с): ${produceTime}$resetColor
		
		""".trimIndent()
	)
}

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
	chart.yAxisTitle = "Количество"

	val xData = DoubleArray(numberOfIntervals) { minValue + it * intervalSize + intervalSize / 2 }
	val yData = histogram.map { it.toDouble() }.toDoubleArray()

	chart.addSeries("Гистограмма", xData, yData)

	val outputDir = mdIfNot("output")
	BitmapEncoder.saveBitmap(chart, "./$outputDir/histogram", BitmapFormat.JPG)
}

fun researchDistributionIdea(statisticsArray: Array<Statistics>) {
	val produceTimeList = statisticsArray.map { it.getProduceTime() }

	val n = produceTimeList.size
	val mean = produceTimeList.average()
	val stdDev = sqrt(produceTimeList.map { (it - mean) * (it - mean) }.sum() / (n - 1))

	val ksTest = KolmogorovSmirnovTest()
	val normalDistribution = NormalDistribution(mean, stdDev)
	val pValue = ksTest.kolmogorovSmirnovTest(normalDistribution, produceTimeList.toDoubleArray())

	val alpha = 0.05

	val isNormal = pValue > alpha

	println("Данные${if (isNormal) "" else " не"} нормально распределены.")
}

fun researchConfidenceInterval(statisticsArrayArray: List<Array<Statistics>>) {
	val simulations = statisticsArrayArray.lastIndex
	val range = 1..simulations

	val intervals = mutableListOf<Pair<Double, Double>>()
	val means = mutableListOf<Double>()

	for (i in range) {
		val statisticsArray = statisticsArrayArray[i]

		val produceTimeList = statisticsArray.map { it.getProduceTime() }

		val n = produceTimeList.size
		val mean = produceTimeList.average()
		val stdDev = sqrt(produceTimeList.map { (it - mean) * (it - mean) }.sum() / (n - 1.0))

		val tDist = TDistribution(n - 1.0)
		val alpha = 0.05
		val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

		val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

		val (from, to) = mean - marginOfError to mean + marginOfError

		intervals.add(from to to)
		means.add(mean)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Доверительные интервалы и средние значения"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Значения"

	chart.addSeries("Доверительный интервал L", range.map { it + 1 }, intervals.map { it.first })
		.setXYSeriesRenderStyle(
			XYSeries.XYSeriesRenderStyle.Line
		).setMarkerColor(
			Color.GRAY
		).setLineColor(
			Color.GRAY
		).fillColor = Color.GRAY

	chart.addSeries("Доверительный интервал U", range.map { it + 1 }, intervals.map { it.second })
		.setXYSeriesRenderStyle(
			XYSeries.XYSeriesRenderStyle.Line
		).setMarkerColor(
			Color.GRAY
		).setLineColor(
			Color.GRAY
		).fillColor = Color.GRAY

	chart.addSeries("Средние значения", range.map { it + 1 }, means).setXYSeriesRenderStyle(
		XYSeries.XYSeriesRenderStyle.Line
	).setMarkerColor(
		Color.BLACK
	).setLineColor(
		Color.BLACK
	).fillColor = Color.BLACK

	val outputDir = mdIfNot("output")
	BitmapEncoder.saveBitmap(chart, "./$outputDir/confidence", BitmapFormat.JPG)
}

fun researchAccuracy(statisticsArrayArray: List<Array<Statistics>>) {
	val simulations = statisticsArrayArray.lastIndex
	val range = 1..simulations

	val averageResults = mutableListOf<Pair<Int, Double>>()

	for (i in range) {
		val statisticsArray = statisticsArrayArray[i]

		val averageAccuracy = statisticsArray.map { abs(it.getProduceTime() - 6.0) }.average()

		averageResults.add(i to averageAccuracy)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Зависимость точности от количества прогонов"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Средняя точность"

	val xData = averageResults.map { it.first + 1.0 }
	val yData = averageResults.map { it.second }

	chart.addSeries("Точность", xData, yData)

	val outputDir = mdIfNot("output")
	BitmapEncoder.saveBitmap(chart, "./$outputDir/accuracy_plot", BitmapFormat.JPG)
}

fun makeOneHundredLaunchs(): Array<Array<Statistics>> {
	val statisticsArrayArray = mutableListOf<Array<Statistics>>()

	for (i in 0..99) {
		val statisticsArray = Array(i + 1) { Statistics() }
		val factoryArray = Array(i + 1) {
			val factory = Factory(1000)
			factory.statistics = statisticsArray[it]
			factory
		}
		val threadArray = Array(i + 1) {
			Thread {
				factoryArray[it].run()
			}
		}

		threadArray.forEach { it.start() }
		threadArray.forEach { it.join() }

		statisticsArrayArray.add(statisticsArray)
	}

	return statisticsArrayArray.toTypedArray()
}

private fun mdIfNot(path: String): File {
	val soundsDir = File(path)
	if (!soundsDir.exists()) {
		soundsDir.mkdirs()
	}
	return soundsDir
}