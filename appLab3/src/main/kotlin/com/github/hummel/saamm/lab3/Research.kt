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

fun researchConfidenceInterval(statisticsArray: Array<Statistics>) {
	val produceTimeList = statisticsArray.map { it.getProduceTime() }

	val intervals = mutableListOf<Pair<Double, Double>>()

	val n = produceTimeList.size
	val mean = produceTimeList.average()
	val stdDev = sqrt(produceTimeList.map { (it - mean) * (it - mean) }.sum() / (n - 1.0))

	val tDist = TDistribution(n - 1.0)
	val alpha = 0.05
	val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

	val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

	val (from, to) = mean - marginOfError to mean + marginOfError

	repeat(statisticsArray.size) {
		intervals.add(from to to)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Доверительные интервалы и средние значения"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Значения"

	chart.addSeries("Доверительный интервал L", statisticsArray.indices.map { it + 1 }, intervals.map { it.first })
		.setXYSeriesRenderStyle(
			XYSeries.XYSeriesRenderStyle.Line
		).setMarkerColor(
			Color.GRAY
		).setLineColor(
			Color.GRAY
		).fillColor = Color.GRAY

	chart.addSeries("Доверительный интервал U", statisticsArray.indices.map { it + 1 }, intervals.map { it.second })
		.setXYSeriesRenderStyle(
			XYSeries.XYSeriesRenderStyle.Line
		).setMarkerColor(
			Color.GRAY
		).setLineColor(
			Color.GRAY
		).fillColor = Color.GRAY

	chart.addSeries(
		"Средние значения", statisticsArray.indices.map { it + 1 }, produceTimeList
	).setXYSeriesRenderStyle(
		XYSeries.XYSeriesRenderStyle.Scatter
	).setMarkerColor(
		Color.BLACK
	).setLineColor(
		Color.BLACK
	).fillColor = Color.BLACK

	val outputDir = mdIfNot("output")
	BitmapEncoder.saveBitmap(chart, "./$outputDir/confidence", BitmapFormat.JPG)
}

fun researchAccuracy(statisticsArrayArray: List<Array<Statistics>>) {
	val range = 1..statisticsArrayArray.lastIndex

	val deltas = mutableListOf<Double>()

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

		val delta = marginOfError
		deltas.add(delta)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Зависимость погрешности от прогонов"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Дельта"

	chart.addSeries("Дельта", range.map { it + 1 }, movingAverage(deltas, 20))

	val outputDir = mdIfNot("output")
	BitmapEncoder.saveBitmap(chart, "./$outputDir/accuracy_plot", BitmapFormat.JPG)
}

fun generateAllSetsOfSimulations(): Array<Array<Statistics>> {
	val statisticsArrayArray = mutableListOf<Array<Statistics>>()

	for (i in 1..100) {
		val statisticsArray = Array(i) { Statistics() }
		val factoryArray = Array(i) {
			val factory = Factory()
			factory.statistics = statisticsArray[it]
			factory
		}
		val threadArray = Array(i) {
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

fun movingAverage(data: List<Double>, windowSize: Int): List<Double> {
	val result = mutableListOf<Double>()
	for (i in data.indices) {
		val start = maxOf(0, i - windowSize / 2)
		val end = minOf(data.size - 1, i + windowSize / 2)
		val average = data.subList(start, end + 1).average()
		result.add(average)
	}
	return result
}

fun generateSetsOfSimulationsForce(addition: Float, generatorChance: Float): Array<Statistics> {
	val statisticsArray = Array(20000 + 50 * 30000) { Statistics() }
	val factoryArray = Array(20000 + 50 * 30000) {
		val factory = Factory(
			addition = addition, generatorChance = generatorChance
		)
		factory.statistics = statisticsArray[it]
		factory
	}
	val threadArray = Array(20000 + 50 * 30000) {
		Thread {
			factoryArray[it].run()
		}
	}

	threadArray.forEach { it.start() }
	threadArray.forEach { it.join() }

	return statisticsArray
}

fun generateSetsOfSimulationsForce50(addition: Float, generatorChance: Float, exitTime: Float): Array<Statistics> {
	val statisticsArray = Array(50) { Statistics() }
	val factoryArray = Array(50) {
		val factory = Factory(
			addition = addition, generatorChance = generatorChance, exitTime = exitTime
		)
		factory.statistics = statisticsArray[it]
		factory
	}
	val threadArray = Array(50) {
		Thread {
			factoryArray[it].run()
		}
	}

	threadArray.forEach { it.start() }
	threadArray.forEach { it.join() }

	return statisticsArray
}

fun generateSetsOfSimulationsForce1(addition: Float, generatorChance: Float, exitTime: Float): Array<Statistics> {
	val statisticsArray = Array(1) { Statistics() }
	val factoryArray = Array(1) {
		val factory = Factory(
			addition = addition, generatorChance = generatorChance, exitTime = exitTime
		)
		factory.statistics = statisticsArray[it]
		factory
	}
	val threadArray = Array(1) {
		Thread {
			factoryArray[it].run()
		}
	}

	threadArray.forEach { it.start() }
	threadArray.forEach { it.join() }

	return statisticsArray
}