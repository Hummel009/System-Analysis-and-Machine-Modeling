package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.CategoryChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
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
	val outputDir = mdIfNot("output")

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

	val n = produceTimeList.size
	val mean = produceTimeList.average()
	val stdDev = sqrt(produceTimeList.map { (it - mean) * (it - mean) }.sum() / (n - 1))

	val tDist = TDistribution((n - 1).toDouble())
	val alpha = 0.05
	val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

	val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

	val (from, to) = mean - marginOfError to mean + marginOfError

	println("Доверительный интервал: [$from, $to]")
}

fun researchAccuracy() {
	val maxRuns = 200
	val step = 1
	val averageResults = mutableListOf<Pair<Int, Double>>()

	for (runs in step..maxRuns step step) {
		val statisticsArray = Array(runs) { Statistics() }

		val factoryArray = Array(runs) {
			val factory = Factory(ceil(runs / 5.0f).toInt())
			factory.statistics = statisticsArray[it]
			factory
		}
		val threadArray = Array(runs) {
			Thread {
				factoryArray[it].run()
			}
		}

		threadArray.forEach { it.start() }
		threadArray.forEach { it.join() }

		val averageAccuracy = statisticsArray.map { it.getProduceTime() }.average()
		averageResults.add(runs to averageAccuracy)
	}

	val chart = XYChart(1600, 900)
	chart.title = "Зависимость точности от количества прогонов"
	chart.xAxisTitle = "Количество прогонов"
	chart.yAxisTitle = "Средняя точность"

	val xData = averageResults.map { it.first.toDouble() }.toDoubleArray()
	val yData = averageResults.map { it.second }.toDoubleArray()

	chart.addSeries("Точность", xData, yData)

	SwingWrapper(chart).displayChart()
}

private fun mdIfNot(path: String): File {
	val soundsDir = File(path)
	if (!soundsDir.exists()) {
		soundsDir.mkdirs()
	}
	return soundsDir
}