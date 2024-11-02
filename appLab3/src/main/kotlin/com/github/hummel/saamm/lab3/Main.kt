package com.github.hummel.saamm.lab3

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.knowm.xchart.XYChart
import java.util.PriorityQueue
import java.util.Random

const val PARTS_1_FOR_PRODUCT = 3
const val PARTS_2_FOR_PRODUCT = 2

const val PRODUCTS_FOR_PACKET = 8
const val PACKETS_FOR_STORAGE = 3

fun main() {
//	val statisticsArrayArray = generateAllSetsOfSimulations()
//
//	researchAverageStats(statisticsArrayArray[99].copyOf())
//
//	researchDistributionGraph(statisticsArrayArray[99].copyOf())
//
//	researchDistributionIdea(statisticsArrayArray[99].copyOf())
//
//	researchConfidenceInterval(statisticsArrayArray[9].copyOf())
//
//	researchAccuracy(statisticsArrayArray.take(100))

	val statisticsArrayOrig = generateSetsOfSimulationsForce(0.0f, 0.5f)
	val statisticsArrayF1 = generateSetsOfSimulationsForce(0.0f, 0.45f)
	val statisticsArrayF2 = generateSetsOfSimulationsForce(0.0f, 0.35f)
	val statisticsArrayF3 = generateSetsOfSimulationsForce(0.0f, 0.25f)
	val statisticsArrayF4 = generateSetsOfSimulationsForce(0.0f, 0.15f)

	val resultsOrig = statisticsArrayOrig.map { it.getProduceTime() }.toDoubleArray()
	val results1 = statisticsArrayF1.map { it.getProduceTime() }.toDoubleArray()
	val results2 = statisticsArrayF2.map { it.getProduceTime() }.toDoubleArray()
	val results3 = statisticsArrayF3.map { it.getProduceTime() }.toDoubleArray()
	val results4 = statisticsArrayF4.map { it.getProduceTime() }.toDoubleArray()

	val correlation = PearsonsCorrelation()

	val tempOrig = resultsOrig.copyOf()
	tempOrig.sort()
	val temp1 = results1.copyOf()
	temp1.sort()
	val temp2 = results2.copyOf()
	temp2.sort()
	val temp3 = results3.copyOf()
	temp3.sort()
	val temp4 = results4.copyOf()
	temp4.sort()
	val correlationCoefficient1 = correlation.correlation(tempOrig, temp1)
	val correlationCoefficient2 = correlation.correlation(tempOrig, temp2)
	val correlationCoefficient3 = correlation.correlation(tempOrig, temp3)
	val correlationCoefficient4 = correlation.correlation(tempOrig, temp4)

	println("Коэффициент корреляции 1: $correlationCoefficient1")
	println("Коэффициент корреляции 2: $correlationCoefficient2")
	println("Коэффициент корреляции 3: $correlationCoefficient3")
	println("Коэффициент корреляции 4: $correlationCoefficient4")

	val chart = XYChart(1600, 900)
	chart.title = "Сравнение результатов"
	chart.xAxisTitle = "Индекс"
	chart.yAxisTitle = "Значение"

	chart.addSeries("Результаты Orig", tempOrig.indices.map { it.toDouble() }.toDoubleArray(), tempOrig)
	chart.addSeries("Результаты 1", temp1.indices.map { it.toDouble() }.toDoubleArray(), temp1)
	chart.addSeries("Результаты 2", temp2.indices.map { it.toDouble() }.toDoubleArray(), temp2)
	chart.addSeries("Результаты 3", temp3.indices.map { it.toDouble() }.toDoubleArray(), temp3)
	chart.addSeries("Результаты 4", temp4.indices.map { it.toDouble() }.toDoubleArray(), temp4)

	chart.addSeries(
		"Регрессия",
		temp1.indices.map { it.toDouble() }.toDoubleArray(),
		approximate(tempOrig, temp1, temp2, temp3, temp4)
	)

	//SwingWrapper(chart).displayChart()
}

class Factory(
	val stopRule: Int = 2000,
	val addition: Float = 0.0f,
	val generatorChance: Float = 0.5f,
	val exitTime: Float = 100000.0f
) {
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

		while (currentTime <= exitTime * 1000) {
			val task = queue.poll()
			currentTime = task.endTime

			when (task.taskType) {
				TaskType.GENERATOR -> {
					val time = (random.nextGaussian().coerceIn(-0.5, 0.5) + 0.5) * 1000 + 500 + addition

					if (random.nextFloat() <= generatorChance) {
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
						statistics.productTimes[(currentTime / 1000).toFloat()] =
							((currentTime / 1000).toFloat() / (storagePackets * 8))
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
	val productTimes = mutableMapOf<Float, Float>()

	fun getProduceTime() = duration / (storagePackets * 8)
}

data class Task(val endTime: Double, val taskType: TaskType)

enum class TaskType {
	GENERATOR, MACHINE_1, MACHINE_2, ASSEMBLER, TRANSPORTER, PACKER
}

fun approximate(vararg arrays: DoubleArray): DoubleArray {
	require(arrays.isNotEmpty()) { "Необходимо передать хотя бы один массив." }
	require(arrays.all { it.size == arrays[0].size }) { "Все массивы должны быть одного размера." }

	val n = arrays[0].size
	val m = arrays.size

	// Суммируем значения по каждому массиву
	val sums = DoubleArray(n) { 0.0 }
	for (i in 0 until n) {
		for (j in 0 until m) {
			sums[i] += arrays[j][i]
		}
	}

	// Находим средние значения
	val averages = sums.map { it / m }

	return averages.toDoubleArray()
}