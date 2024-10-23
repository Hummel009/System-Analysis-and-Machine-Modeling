package com.github.hummel.saamm.lab2

fun printAverageStatistics(stats: Array<Statistics>) {
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