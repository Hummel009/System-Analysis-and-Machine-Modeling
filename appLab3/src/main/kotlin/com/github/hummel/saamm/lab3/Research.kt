package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.CategoryChart
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
		Время на производство одного изделия (с): ${duration / (storagePackets * 8)}$resetColor
		
		""".trimIndent()
	)
}

fun researchDistributionIdea(statisticsArray: Array<Statistics>) {
	val timesPerProduct = statisticsArray.map { it.duration / (it.storagePackets * 8) }.toDoubleArray()

	val ksTest = KolmogorovSmirnovTest()
	val normalDistribution = NormalDistribution(6.0, 1.0)
	val pValue = ksTest.kolmogorovSmirnovTest(normalDistribution, timesPerProduct)

	val alpha = 0.05

	val isNormal = pValue > alpha

	println("Данные${if (isNormal) " не" else ""} нормально распределены.")
}

fun researchDistributionGraph(statisticsArray: Array<Statistics>) {
	val outputDir = mdIfNot("output")

	val timesPerProduct = statisticsArray.map { it.duration / (it.storagePackets * 8) }

	val numberOfIntervals = ceil(log2(timesPerProduct.size.toDouble())).toInt() + 1

	val minValue = timesPerProduct.minOrNull() ?: 0.0
	val maxValue = timesPerProduct.maxOrNull() ?: 1.0
	val intervalSize = (maxValue - minValue) / numberOfIntervals

	val histogram = IntArray(numberOfIntervals)
	timesPerProduct.forEach { time ->
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

fun researchConfidenceInterval(statisticsArray: Array<Statistics>) {
	val timesPerProduct = statisticsArray.map { it.duration / (it.storagePackets * 8) }

	val n = timesPerProduct.size
	val mean = timesPerProduct.average()
	val stdDev = sqrt(timesPerProduct.map { (it - mean) * (it - mean) }.sum() / (n - 1))

	val tDist = TDistribution((n - 1).toDouble())
	val alpha = 0.05
	val tValue = tDist.inverseCumulativeProbability(1 - alpha / 2)

	val marginOfError = tValue * (stdDev / sqrt(n.toDouble()))

	val (from, to) = mean - marginOfError to mean + marginOfError

	println("Доверительный интервал: [$from, $to]")
}

private fun mdIfNot(path: String): File {
	val soundsDir = File(path)
	if (!soundsDir.exists()) {
		soundsDir.mkdirs()
	}
	return soundsDir
}