package com.github.hummel.saamm.lab4

fun compareAlternatives() {
	val alternatives = arrayOf(0.3, 0.5, 0.7)
	val results = alternatives.map { alternative ->
		simulateRuns(30, generatorChance = alternative).map { it.getProduceTime() }
	}

	performANOVA(results)
}

fun performANOVA(data: List<List<Double>>) {
}