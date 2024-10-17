package com.github.hummel.saamm.lab2

import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

const val NUM_PARTS_TYPE_1 = 3
const val NUM_PARTS_TYPE_2 = 2
const val PACKET_SIZE = 8
const val PACKETS = 3

fun main() {
	val factories = Array(10) { Factory() }
	val statisticsList = Array(10) { Statistics() }

	val threads = factories.mapIndexed { index, factory ->
		Thread {
			factory.run()
			statisticsList[index] = factory.statistics
		}
	}

	threads.forEach { it.start() }
	threads.forEach { it.join() }

	printAverageStatistics(statisticsList)
}

fun printAverageStatistics(statisticsList: Array<Statistics>) {
	val totalProducedParts1 = statisticsList.sumOf { it.producedParts1.get() }
	val totalProducedParts2 = statisticsList.sumOf { it.producedParts2.get() }
	val totalProcessedParts1 = statisticsList.sumOf { it.processedParts1.get() }
	val totalProcessedParts2 = statisticsList.sumOf { it.processedParts2.get() }
	val totalAssembledProducts = statisticsList.sumOf { it.assembledProducts.get() }
	val totalPackedPackets = statisticsList.sumOf { it.packedPackets.get() }
	val totalStoragePackets = statisticsList.sumOf { it.storagePackets.get() }
	val totalSeconds = statisticsList.sumOf { it.seconds }

	val numberOfFactories = statisticsList.size

	val redColor = "\u001B[31m"
	val resetColor = "\u001B[0m"

	println(
		"""
		${redColor}Средняя статистика по $numberOfFactories заводам:
		Создано деталей A: ${totalProducedParts1 / numberOfFactories},
		Создано деталей B: ${totalProducedParts2 / numberOfFactories},
		Обработано деталей A: ${totalProcessedParts1 / numberOfFactories},
		Обработано деталей B: ${totalProcessedParts2 / numberOfFactories},
		Собрано изделий: ${totalAssembledProducts / numberOfFactories},
		Собрано партий: ${totalPackedPackets / numberOfFactories},
		Партий на складе: ${totalStoragePackets / numberOfFactories},
		Общее время (с): ${totalSeconds / numberOfFactories},
		Время на производство одного изделия (с): ${totalSeconds / totalAssembledProducts}$resetColor
		""".trimIndent()
	)
}

class Factory {
	private val partsType1 = AtomicInteger(0)
	private val partsType2 = AtomicInteger(0)
	private val accumulatorPartsType1 = AtomicInteger(0)
	private val accumulatorPartsType2 = AtomicInteger(0)
	private val technoModuleParts = AtomicInteger(0)
	private val packPlaceProducts = AtomicInteger(0)
	private val packPlacePackets = AtomicInteger(0)
	private val storagePackets = AtomicInteger(0)

	private val random = Random()
	private var currentTime = AtomicLong(0)

	val statistics = Statistics()

	fun run() {
		val threads = mutableListOf(Thread { partGenerator() },
			Thread { machine(1, partsType1, accumulatorPartsType1) },
			Thread { machine(2, partsType2, accumulatorPartsType2) },
			Thread { assembler() },
			Thread { packer() },
			Thread { transporter() })
		threads.forEach { it.start() }
		threads.forEach { it.join() }

		traceState()
	}

	private fun partGenerator() {
		while (getStopRule()) {
			Thread.sleep(1)

			val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
			currentTime.getAndAdd(time)

			if (random.nextBoolean()) {
				partsType1.incrementAndGet()

				statistics.incrementProducedParts(1)
			} else {
				partsType2.incrementAndGet()

				statistics.incrementProducedParts(2)
			}
		}
	}

	private fun machine(type: Int, parts: AtomicInteger, accumulator: AtomicInteger) {
		while (getStopRule()) {
			Thread.sleep(1)

			if (parts.get() > 0) {
				parts.decrementAndGet()

				val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
				currentTime.getAndAdd(time)

				accumulator.incrementAndGet()

				statistics.incrementProcessedParts(type)
			}
		}
	}

	private fun assembler() {
		while (getStopRule()) {
			Thread.sleep(1)

			if (technoModuleParts.get() >= NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					technoModuleParts.decrementAndGet()
				}

				val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
				currentTime.getAndAdd(time)

				packPlaceProducts.incrementAndGet()

				statistics.incrementAssembledProducts()
			}
		}
	}

	private fun packer() {
		while (getStopRule()) {
			Thread.sleep(1)

			if (packPlaceProducts.get() >= PACKET_SIZE) {
				repeat(PACKET_SIZE) {
					packPlaceProducts.decrementAndGet()
				}

				val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
				currentTime.getAndAdd(time)

				packPlacePackets.incrementAndGet()

				statistics.incrementPackedPackets()
			}
		}
	}

	private fun transporter() {
		while (getStopRule()) {
			Thread.sleep(1)

			if (accumulatorPartsType1.get() >= NUM_PARTS_TYPE_1 && accumulatorPartsType2.get() >= NUM_PARTS_TYPE_2) {
				repeat(NUM_PARTS_TYPE_1) {
					accumulatorPartsType1.decrementAndGet()
				}
				repeat(NUM_PARTS_TYPE_2) {
					accumulatorPartsType2.decrementAndGet()
				}

				val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
				currentTime.getAndAdd(time)

				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					technoModuleParts.incrementAndGet()
				}
			}

			if (packPlacePackets.get() >= PACKETS) {
				repeat(PACKETS) {
					packPlacePackets.decrementAndGet()
				}

				val time = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toLong()
				currentTime.getAndAdd(time)

				repeat(PACKETS) {
					storagePackets.incrementAndGet()

					statistics.incrementStoragePackets()
				}
			}
		}
	}

	private fun getStopRule(): Boolean = storagePackets.get() < 100

	fun traceState() {
		statistics.printStatistics(currentTime)
		println()
	}
}

class Statistics {
	val producedParts1 = AtomicInteger(0)
	val producedParts2 = AtomicInteger(0)
	val processedParts1 = AtomicInteger(0)
	val processedParts2 = AtomicInteger(0)
	val assembledProducts = AtomicInteger(0)
	val packedPackets = AtomicInteger(0)
	val storagePackets = AtomicInteger(0)
	var seconds = 0

	fun incrementProducedParts(i: Int) {
		if (i == 1) {
			producedParts1.incrementAndGet()
		} else {
			producedParts2.incrementAndGet()
		}
	}

	fun incrementProcessedParts(i: Int) {
		if (i == 1) {
			processedParts1.incrementAndGet()
		} else {
			processedParts2.incrementAndGet()
		}
	}

	fun incrementAssembledProducts() {
		assembledProducts.incrementAndGet()
	}

	fun incrementPackedPackets() {
		packedPackets.incrementAndGet()
	}

	fun incrementStoragePackets() {
		storagePackets.incrementAndGet()
	}

	fun printStatistics(currentTime: AtomicLong) {
		seconds = (currentTime.get() / 2000).toInt()

		println(
			"""
			Статистика:
			Создано деталей A: ${producedParts1.get()},
			Создано деталей B: ${producedParts2.get()},
			Обработано деталей A: ${processedParts1.get()},
			Обработано деталей B: ${processedParts2.get()},
			Собрано изделий: ${assembledProducts.get()},
			Собрано партий: ${packedPackets.get()},
			Партий на складе: ${storagePackets.get()},
			Общее время (с): $seconds,
			Время на производство одного изделия (с): ${seconds / assembledProducts.get()}
			""".trimIndent()
		)
	}
}