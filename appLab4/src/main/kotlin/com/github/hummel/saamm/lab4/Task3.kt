package com.github.hummel.saamm.lab4

import java.io.File

fun research2fExperiment(statisticsArrayArray: Array<Array<Statistics>>) {
	val graphs = statisticsArrayArray.map {
		it.map { stat -> stat.getProduceTime() }.toDoubleArray().apply { sort() }
	}

	for (i in graphs.indices) {
		val xValues = graphs[i].indices.map { it.toDouble() }
		val yValues = graphs[i].toList()

		File("$outputDir/task3line${i + 1}-x.txt").printWriter().use { out ->
			xValues.forEach { out.println(it) }
		}

		File("$outputDir/task3line${i + 1}-y.txt").printWriter().use { out ->
			yValues.forEach { out.println(it) }
		}
	}
}