package com.github.hummel.saamm.lab3

import org.apache.commons.math3.distribution.FDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.sqrt

fun researchContinousRunPossibility(statisticsArrayArray: Array<Array<Statistics>>, cutoff: Int) {
	val longValueSeries = statisticsArrayArray[0].flatMap { statistics ->
		statistics.productTimes.filter { (key, _) -> key > cutoff }.values
	}.chunked(statisticsArrayArray[1].size).flatten().toDoubleArray()

	val shortValueSeries = statisticsArrayArray[1].flatMap { statistics ->
		statistics.productTimes.filter { (key, _) -> key > cutoff }.values
	}.toDoubleArray()

	val description1 = DescriptiveStatistics(longValueSeries)
	val description2 = DescriptiveStatistics(shortValueSeries)

	val df = DecimalFormat("#.#####")

	val n1 = description1.n
	val mean1 = description1.mean
	val variance1 = description1.variance

	val n2 = description2.n
	val mean2 = description2.mean
	val variance2 = description2.variance

	println("Дисперсия первой выборки: ${df.format(variance1)}")
	println("Дисперсия второй выборки: ${df.format(variance2)}")

	println("Срзнач первой выборки: ${df.format(mean1)}")
	println("Срзнач второй выборки: ${df.format(mean2)}")

	val fStatistic = variance1 / variance2
	println("F-статистика: ${df.format(fStatistic)}")

	val pooledVariance = ((n1 - 1) * variance1 + (n2 - 1) * variance2) / (n1 + n2 - 2)
	val tStatistic = (mean1 - mean2) / sqrt(pooledVariance * (1.0 / n1 + 1.0 / n2))
	println("t-статистика: ${df.format(tStatistic)}")

	val dfFisher = n1 + n2 - 2
	println("Степени свободы для t-теста: $dfFisher")

	val alpha = 0.05

	val criticalT = getCriticalT(dfFisher, alpha)
	println("Критическое значение t: ${df.format(criticalT)}")

	val criticalF = getCriticalF(n1 - 1, n2 - 1, alpha)
	println("Критическое значение F: ${df.format(criticalF)}")

	if (abs(tStatistic) > criticalT) {
		println("Гипотеза о равенстве средних принимамется.")
	} else {
		println("Гипотеза о равенстве средних отвергается.")
	}

	if (fStatistic > criticalF) {
		println("Гипотеза о равенстве дисперсий принимамется.")
	} else {
		println("Гипотеза о равенстве дисперсий отвергается.")
	}
}

private fun getCriticalT(df: Long, alpha: Double): Double {
	val tDistribution = TDistribution(df.toDouble())
	return tDistribution.inverseCumulativeProbability(1 - alpha / 2)
}

private fun getCriticalF(df1: Long, df2: Long, alpha: Double): Double {
	val fDistribution = FDistribution(df1.toDouble(), df2.toDouble())
	return fDistribution.inverseCumulativeProbability(1 - alpha)
}