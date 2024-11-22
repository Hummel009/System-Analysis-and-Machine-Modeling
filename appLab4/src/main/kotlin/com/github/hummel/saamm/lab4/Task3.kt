package com.github.hummel.saamm.lab4

import java.io.File
import java.text.DecimalFormat

fun research2fExperiment(statisticsArrayArray: Array<Array<Statistics>>) {
	val zData = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.average()
	}.toDoubleArray()
	val xData = DoubleArray(zData.size) { i -> (i + 1) * 0.1 }
	val yData = DoubleArray(zData.size) { i -> (i + 1) * 10.0 }

	val df = DecimalFormat("#.##")

	File("$outputDir/task3line-x.txt").printWriter().use { out ->
		xData.forEach { out.println(df.format(it)) }
	}

	File("$outputDir/task3line-y.txt").printWriter().use { out ->
		yData.forEach { out.println(df.format(it)) }
	}

	File("$outputDir/task3line-z.txt").printWriter().use { out ->
		zData.forEach { out.println(df.format(it)) }
	}
}