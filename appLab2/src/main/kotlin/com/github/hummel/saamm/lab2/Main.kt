package com.github.hummel.saamm.lab2

import java.util.PriorityQueue
import java.util.Random

const val PARTS_1_FOR_PRODUCT = 3
const val PARTS_2_FOR_PRODUCT = 2

const val PRODUCTS_FOR_PACKET = 8
const val PACKETS_FOR_STORAGE = 3

fun main() {
	val statisticsArray = Array(10) { Statistics() }
	val factoryArray = Array(10) {
		val factory = Factory()
		factory.statistics = statisticsArray[it]
		factory
	}
	val threadArray = Array(10) {
		Thread {
			factoryArray[it].run()
		}
	}

	threadArray.forEach { it.start() }
	threadArray.forEach { it.join() }

	printAverageStatistics(statisticsArray)
}

class Factory {
	private val random = Random()

	private var partsType1 = 0
	private var partsType2 = 0
	private var accumulatorPartsType1 = 0
	private var accumulatorPartsType2 = 0
	private var technoModuleParts = 0
	private var packPlaceProducts = 0
	private var packPlacePackets = 0
	private var storagePackets = 0

	var statistics = Statistics()

	fun run() {
		val queue = PriorityQueue<Task>(compareBy { it.endTime })
		var currentTime = 0.0

		queue.add(Task(currentTime, TaskType.GENERATOR))

		while (storagePackets <= 1000) {
			val task = queue.poll()
			currentTime = task.endTime

			when (task.taskType) {
				TaskType.GENERATOR -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (random.nextBoolean()) {
						partsType1++
						statistics.partsType1++
						queue.add(Task(currentTime + time, TaskType.MACHINE_1))
					} else {
						partsType2++
						statistics.partsType2++
						queue.add(Task(currentTime + time, TaskType.MACHINE_2))
					}

					queue.add(Task(currentTime + time, TaskType.GENERATOR))
				}

				TaskType.MACHINE_1 -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (partsType1 >= 1) {
						partsType1--
						accumulatorPartsType1++
						statistics.accumulatorPartsType1++

						queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
					}
				}

				TaskType.MACHINE_2 -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (partsType2 >= 1) {
						partsType2--
						accumulatorPartsType2++
						statistics.accumulatorPartsType2++

						queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
					}
				}

				TaskType.ASSEMBLER -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (technoModuleParts >= PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT) {
						technoModuleParts -= PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT
						packPlaceProducts++
						statistics.packPlaceProducts++

						queue.add(Task(currentTime + time, TaskType.PACKER))
					}
				}

				TaskType.TRANSPORTER -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (accumulatorPartsType1 >= PARTS_1_FOR_PRODUCT && accumulatorPartsType2 >= PARTS_2_FOR_PRODUCT) {
						accumulatorPartsType1 -= PARTS_1_FOR_PRODUCT
						accumulatorPartsType2 -= PARTS_2_FOR_PRODUCT

						technoModuleParts += PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT

						queue.add(Task(currentTime + time, TaskType.ASSEMBLER))
					}

					if (packPlacePackets >= PACKETS_FOR_STORAGE) {
						packPlacePackets -= PACKETS_FOR_STORAGE
						storagePackets += PACKETS_FOR_STORAGE
						statistics.storagePackets += PACKETS_FOR_STORAGE
					}
				}

				TaskType.PACKER -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500

					if (packPlaceProducts >= PRODUCTS_FOR_PACKET) {
						packPlaceProducts -= PRODUCTS_FOR_PACKET
						packPlacePackets++
						statistics.packPlacePackets++

						queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
					}
				}
			}
		}

		statistics.duration = (currentTime / 1000).toDouble()
	}
}

class Statistics {
	var partsType1 = 0
	var partsType2 = 0
	var accumulatorPartsType1 = 0
	var accumulatorPartsType2 = 0
	var packPlaceProducts = 0
	var packPlacePackets = 0
	var storagePackets = 0
	var duration = 0.0

	fun getProduceTime() = duration / (storagePackets * 8)
}

data class Task(val endTime: Double, val taskType: TaskType)

enum class TaskType {
	GENERATOR, MACHINE_1, MACHINE_2, ASSEMBLER, TRANSPORTER, PACKER
}