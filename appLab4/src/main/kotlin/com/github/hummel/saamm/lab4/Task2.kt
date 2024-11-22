package com.github.hummel.saamm.lab4

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.CategoryChart

fun researchComparison(statisticsArrayArray: Array<Array<Statistics>>) {
	val yData = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.average() - 6
	}.toDoubleArray()

	val chart = CategoryChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Параметры: время работы генератора, время работы станков"
	chart.yAxisTitle = "Отклик: время производства изделия"

	val xData = DoubleArray(yData.size) { it + 1.0 }

	chart.addSeries("Оригинальные данные", xData, yData)

	BitmapEncoder.saveBitmap(chart, "./$outputDir/task2", BitmapFormat.JPG)
}