package com.github.hummel.saamm.lab2

import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

const val NUM_PARTS_TYPE_1 = 3
const val NUM_PARTS_TYPE_2 = 2
const val PACKET_SIZE = 8
const val PACKETS = 3

fun main() {
	val factory = Factory()
	factory.run()

	factory.traceState()
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

	private val statistics = Statistics()
	private val random = Random()

	private var currentTime = 0L

	fun run() {
		val threads = mutableListOf(
			Thread { partGenerator() },
			Thread { machine(1, partsType1, accumulatorPartsType1) },
			Thread { machine(2, partsType2, accumulatorPartsType2) },
			Thread { assembler() },
			Thread { packer() },
			Thread { transporter() }
		)
		threads.forEach { it.start() }
		threads.forEach { it.join() }
	}

	private fun partGenerator() {
		while (getStopRule()) {
			Thread.sleep(1)

			val partTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
			currentTime += partTime

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

				val processTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
				currentTime += processTime

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

				val assemblyTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
				currentTime += assemblyTime

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

				val packingTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
				currentTime += packingTime

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

				val transportTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
				currentTime += transportTime

				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					technoModuleParts.incrementAndGet()
				}
			}

			if (packPlacePackets.get() >= PACKETS) {
				repeat(PACKETS) {
					packPlacePackets.decrementAndGet()
				}

				val warehouseTime = ((random.nextGaussian() + 0.5).coerceIn(0.0, 1.0) * 1000 + 500).toInt()
				currentTime += warehouseTime

				repeat(PACKETS) {
					storagePackets.incrementAndGet()

					statistics.incrementStoragePackets()
				}
			}
		}
	}

	private fun getStopRule(): Boolean = storagePackets.get() <= 10

	fun traceState() {
		statistics.printStatistics(currentTime)
	}
}

class Statistics {
	private val producedParts1 = AtomicInteger(0)
	private val producedParts2 = AtomicInteger(0)
	private val processedParts1 = AtomicInteger(0)
	private val processedParts2 = AtomicInteger(0)
	private val assembledProducts = AtomicInteger(0)
	private val packedPackets = AtomicInteger(0)
	private val storagePackets = AtomicInteger(0)

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

	fun printStatistics(currentTime: Long) {
		val seconds = (currentTime / 1000).toInt()

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
			Время на производство одной детали (с): ${seconds / assembledProducts.get()}
			""".trimIndent()
		)
	}
}