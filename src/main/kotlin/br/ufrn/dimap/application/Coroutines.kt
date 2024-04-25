package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService

fun main() {
    val checkpoint1 = System.currentTimeMillis()

    ExecutionService.importUsingCoroutines()

    val checkpoint2 = System.currentTimeMillis()

    ExecutionService.interpolateUsingCoroutines()

    val checkpoint3 = System.currentTimeMillis()

    ExecutionService.exportUsingCoroutines()

    val checkpoint4 = System.currentTimeMillis()

    ExecutionService.printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}