package com.github.hummel.saamm.lab2

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

const val NUM_PARTS_TYPE_1 = 3
const val NUM_PARTS_TYPE_2 = 2
const val PARTS_PER_BATCH = 8 * 3

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
	private val partsStorage = AtomicInteger(0)
	private val finishedProducts = AtomicInteger(0)

	private val statistics = Statistics()
	private val startTime = System.currentTimeMillis()

	suspend fun run() {
		coroutineScope {
			launch { partGenerator() }
			launch { machine(1, partsType1) }
			launch { machine(2, partsType2) }
			launch { assembler() }
			launch { transporter() }
		}
	}

	// Источник деталей
	private suspend fun partGenerator() {
		while (true) {
			delay(1000)
			if (Random.nextBoolean()) {
				partsType1.incrementAndGet()
				println("Сгенерирована деталь типа 1. Всего: ${partsType1.get()}")
			} else {
				partsType2.incrementAndGet()
				println("Сгенерирована деталь типа 2. Всего: ${partsType2.get()}")
			}
			traceState()
		}
	}

	// Станок
	private suspend fun machine(type: Int, parts: AtomicInteger) {
		while (true) {
			if (parts.get() > 0) {
				parts.decrementAndGet()
				delay(1000)
				partsStorage.incrementAndGet()
				println("Станок $type обработал деталь. Хранилище деталей: ${partsStorage.get()}")
				statistics.incrementProcessedParts()
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	// Сборщик
	private suspend fun assembler() {
		while (true) {
			if (partsStorage.get() >= NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) {
				delay(1000)
				finishedProducts.incrementAndGet()
				println("Собрано изделие. Всего изделий: ${finishedProducts.get()}")
				statistics.incrementFinishedProducts()
				repeat(NUM_PARTS_TYPE_1 + NUM_PARTS_TYPE_2) { partsStorage.decrementAndGet() }
			} else {
				delay(500) // Снижаем нагрузку на CPU
			}
		}
	}

	// Транспортировщик
	private suspend fun transporter() {
		while (true) {
			if (finishedProducts.get() >= PARTS_PER_BATCH) {
				delay(1000)
				finishedProducts.addAndGet(-PARTS_PER_BATCH)
				println("Отправлено на склад: $PARTS_PER_BATCH изделий. Оставшиеся: ${finishedProducts.get()}")
				statistics.incrementBatchesSent()
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
	private val batchesSent = AtomicInteger(0)

	fun incrementProcessedParts() {
		processedParts.incrementAndGet()
	}

	fun incrementFinishedProducts() {
		finishedProducts.incrementAndGet()
	}

	fun incrementBatchesSent() {
		batchesSent.incrementAndGet()
	}

	fun printStatistics(startTime: Long) {
		val currentTime = System.currentTimeMillis()
		val elapsedTimeInMinutes = (currentTime - startTime) / 1000 // Время работы в секундах
		val averageTimePerAssembly =
			if (finishedProducts.get() > 0) elapsedTimeInMinutes / finishedProducts.get() else 0

		val redColor = "\u001B[31m" // Код для красного цвета
		val resetColor = "\u001B[0m" // Код для сброса цвета

		println(
			"${redColor}Статистика: Обработано деталей: ${processedParts.get()}, Собрано изделий: ${finishedProducts.get()}, " + "Отправлено партий: ${batchesSent.get()}, Среднее время на сборку: $averageTimePerAssembly сек${resetColor}"
		)
	}
}