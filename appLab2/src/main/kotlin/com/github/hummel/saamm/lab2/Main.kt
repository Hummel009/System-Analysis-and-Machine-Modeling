package com.github.hummel.saamm.lab2

import java.util.PriorityQueue
import java.util.Random

private val random = Random()

data class Task(val endTime: Float, val taskType: String)

fun main() {
	val queue = PriorityQueue<Task>(compareBy { it.endTime })
	var currentTime = 0f

	queue.add(Task(currentTime, "Generator"))

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
			"Generator" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (random.nextBoolean()) {
					partsType1++
					queue.add(Task(currentTime + time, "Machine1"))
				} else {
					partsType2++
					queue.add(Task(currentTime + time, "Machine2"))
				}

				queue.add(Task(currentTime + time, "Generator"))
			}

			"Machine1" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (partsType1 >= 1) {
					partsType1--
					accumulatorPartsType1++

					queue.add(Task(currentTime + time, "Transporter"))
				}

				queue.add(Task(currentTime + time, "Machine1"))
			}

			"Machine2" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (partsType2 >= 1) {
					partsType2--
					accumulatorPartsType2++

					queue.add(Task(currentTime + time, "Transporter"))
				}

				queue.add(Task(currentTime + time, "Machine2"))
			}

			"Assembler" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (technoModuleParts >= 5) {
					technoModuleParts -= 5
					packPlaceProducts += 1

					queue.add(Task(currentTime + time, "Packer"))
				}

				queue.add(Task(currentTime + time, "Assembler"))
			}

			"Transporter" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (accumulatorPartsType1 >= 3 && accumulatorPartsType2 >= 2) {
					accumulatorPartsType1 -= 3
					accumulatorPartsType2 -= 2

					technoModuleParts += 5

					queue.add(Task(currentTime + time, "Assembler"))
				}

				if (packPlacePackets >= 3) {
					packPlacePackets -= 3
					storagePackets += 3
				}

				queue.add(Task(currentTime + time, "Transporter"))
			}

			"Packer" -> {
				val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5).toFloat() * 1000 + 500

				if (packPlaceProducts >= 8) {
					packPlaceProducts -= 8
					packPlacePackets += 1

					queue.add(Task(currentTime + time, "Transporter"))
				}

				queue.add(Task(currentTime + time, "Packer"))
			}
		}
	}

	println("Time: ${(currentTime / 1000).toInt()}s")
	println("Time per product: ${(currentTime / (storagePackets * 8000)).toInt()}s")
}