package com.github.hummel.saamm.lab2

import java.util.PriorityQueue
import java.util.Random

const val PARTS_1_FOR_PRODUCT = 3
const val PARTS_2_FOR_PRODUCT = 2

const val PRODUCTS_FOR_PACKET = 8
const val PACKETS_FOR_STORAGE = 3

private val random = Random()

fun main() {
	val queue = PriorityQueue<Task>(compareBy { it.endTime })
	var currentTime = 0f

	queue.add(Task(currentTime, TaskType.GENERATOR))

	var partsType1 = 0
	var partsType2 = 0
	var accumulatorPartsType1 = 0
	var accumulatorPartsType2 = 0
	var technoModuleParts = 0
	var packPlaceProducts = 0
	var packPlacePackets = 0
	var storagePackets = 0

	while (storagePackets <= 10) {
		val task = queue.poll()
		currentTime = task.endTime

		when (task.taskType) {
			TaskType.GENERATOR -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (random.nextBoolean()) {
					partsType1++
					queue.add(Task(currentTime + time, TaskType.MACHINE_1))
				} else {
					partsType2++
					queue.add(Task(currentTime + time, TaskType.MACHINE_2))
				}

				queue.add(Task(currentTime + time, TaskType.GENERATOR))
			}

			TaskType.MACHINE_1 -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (partsType1 >= 1) {
					partsType1--
					accumulatorPartsType1++

					queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
				}
			}

			TaskType.MACHINE_2 -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (partsType2 >= 1) {
					partsType2--
					accumulatorPartsType2++

					queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
				}
			}

			TaskType.ASSEMBLER -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (technoModuleParts >= PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT) {
					technoModuleParts -= PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT
					packPlaceProducts += 1

					queue.add(Task(currentTime + time, TaskType.PACKER))
				}
			}

			TaskType.TRANSPORTER -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (accumulatorPartsType1 >= PARTS_1_FOR_PRODUCT && accumulatorPartsType2 >= PARTS_2_FOR_PRODUCT) {
					accumulatorPartsType1 -= PARTS_1_FOR_PRODUCT
					accumulatorPartsType2 -= PARTS_2_FOR_PRODUCT

					technoModuleParts += PARTS_1_FOR_PRODUCT + PARTS_2_FOR_PRODUCT

					queue.add(Task(currentTime + time, TaskType.ASSEMBLER))
				}

				if (packPlacePackets >= PACKETS_FOR_STORAGE) {
					packPlacePackets -= PACKETS_FOR_STORAGE
					storagePackets += PACKETS_FOR_STORAGE
				}
			}

			TaskType.PACKER -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (packPlaceProducts >= PRODUCTS_FOR_PACKET) {
					packPlaceProducts -= PRODUCTS_FOR_PACKET
					packPlacePackets += 1

					queue.add(Task(currentTime + time, TaskType.TRANSPORTER))
				}
			}
		}
	}

	println("Time: ${(currentTime / 1000).toInt()}s")
	println("Time per product: ${(currentTime / (storagePackets * 8000)).toInt()}s")
}

data class Task(val endTime: Float, val taskType: TaskType)

enum class TaskType {
	GENERATOR, MACHINE_1, MACHINE_2, ASSEMBLER, TRANSPORTER, PACKER
}