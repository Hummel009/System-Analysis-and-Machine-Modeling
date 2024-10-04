package com.github.hummel.saamm.lab2

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

const val NUM_PARTS_TYPE_1 = 3
const val NUM_PARTS_TYPE_2 = 2
const val PACKET_SIZE = 8
const val PACKETS = 3

fun main() = runBlocking {
	val factory = Factory()
	factory.run()
}

/*
	Концептуальная модель

	Элементы модели:

	1) Источник деталей: Генерирует детали двух типов.
	2) Станки: Обрабатывают детали каждого типа и помещают их в накопитель.
	3) Накопитель: Сохраняет обработанные детали, пока не сформируется комплект для сборки изделия.
	4) Сборка: Собирает изделия из деталей, используя необходимые количества каждого типа.
	5) Транспортный робот: Перемещает изделия на склад после сборки.

	Взаимодействие элементов:

	Часть 1 (Генерация): Источник деталей генерирует детали и передаёт их на станки.
	Часть 2 (Обработка): Станки обрабатывают детали и помещают их в накопитель.
	Часть 3 (Сборка): Накопитель передаёт детали на сборку, которая формирует изделия.
	Часть 4 (Транспортировка): Транспортный робот отправляет готовые изделия на склад.
*/

class Factory {
	private val partsType1 = AtomicInteger(0)
	private val partsType2 = AtomicInteger(0)
	private val accumulator = AtomicInteger(0)
	private val technoModule = AtomicInteger(0)
	private val packModule = AtomicInteger(0)
	private val finishedPackets = AtomicInteger(0)

	private val statistics = Statistics()
	private val startTime = System.currentTimeMillis()

	suspend fun run() {
		coroutineScope {
			launch { partGenerator() }
			launch { machine(1, partsType1) }
			launch { machine(2, partsType2) }
			launch { assembler() }
			launch { packer() }
			launch { transporter() }
		}
	}

	// Источник деталей
	private suspend fun partGenerator() {
		while (true) {
			delay(1000)
			if (Random.nextBoolean()) {
				partsType1.incrementAndGet()
				println("Сгенерирована деталь типа 1. Всего деталей: ${partsType1.get()}")
			} else {
				partsType2.incrementAndGet()
				println("Сгенерирована деталь типа 2. Всего деталей: ${partsType2.get()}")
			}
			traceState()
		}
	}

	// Станок, обрабатывающий детали
	private suspend fun machine(type: Int, parts: AtomicInteger) {
		while (true) {
			if (parts.get() > 0) {
				parts.decrementAndGet()
				delay(1000)
				accumulator.incrementAndGet()
				println("Станок $type обработал деталь. Деталей в накопителе: ${accumulator.get()}")
				statistics.incrementProcessedParts()
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	// Сборщик изделий из деталей в технологическом модуле
	private suspend fun assembler() {
		while (true) {
			if (technoModule.get() >= NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					technoModule.decrementAndGet()
				}
				delay(1000)
				packModule.incrementAndGet()
				println("Собрано изделие. Всего изделий: ${packModule.get()}")
				statistics.incrementFinishedProducts()
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	// Сборщик изделий в технологическом модуле
	private suspend fun packer() {
		while (true) {
			if (packModule.get() >= PACKET_SIZE) {
				repeat(PACKET_SIZE) {
					packModule.decrementAndGet()
				}
				delay(1000)
				finishedPackets.incrementAndGet()
				println("Собрана партия. Всего партий: ${finishedPackets.get()}")
				statistics.incrementFinishedPackets()
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	// Транспортировщик из накопителя в модуль сборки и из модуля компоновки на склад
	private suspend fun transporter() {
		while (true) {
			if (accumulator.get() >= NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					accumulator.decrementAndGet()
				}
				delay(1000)
				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
					technoModule.incrementAndGet()
				}
				println("Перемещено в технологический модуль: ${NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2} деталей.")
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}

			if (finishedPackets.get() >= PACKETS) {
				repeat(PACKETS) {
					finishedPackets.decrementAndGet()
				}
				delay(1000)
				println("Перемещено на склад: $PACKETS партий изделий.")
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	private fun traceState() {
		statistics.printStatistics(startTime)
	}
}

class Statistics {
	private val processedParts = AtomicInteger(0)
	private val finishedProducts = AtomicInteger(0)
	private val finishedPackets = AtomicInteger(0)

	fun incrementProcessedParts() {
		processedParts.incrementAndGet()
	}

	fun incrementFinishedProducts() {
		finishedProducts.incrementAndGet()
	}

	fun incrementFinishedPackets() {
		finishedPackets.incrementAndGet()
	}

	fun printStatistics(startTime: Long) {
		val currentTime = System.currentTimeMillis()
		val elapsedTimeInMinutes = (currentTime - startTime) / 1000
		val averageTimePerAssembly =
			if (finishedProducts.get() > 0) elapsedTimeInMinutes / finishedProducts.get() else 0

		val redColor = "\u001B[31m"
		val resetColor = "\u001B[0m"

		println(
			"${redColor}Статистика: Обработано деталей: ${processedParts.get()}," +
					"Собрано изделий: ${finishedProducts.get()}, " +
					"Собрано партий: ${finishedPackets.get()}, " +
					"Среднее время на сборку: $averageTimePerAssembly сек${resetColor}"
		)
	}
}