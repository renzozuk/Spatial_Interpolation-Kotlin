package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.importUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.interpolateUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.printResult

fun main() {
    importUsingCoroutines()

    val checkpoint1 = System.currentTimeMillis()

    interpolateUsingCoroutines()

    val checkpoint2 = System.currentTimeMillis()

    exportUsingCoroutines()

    printResult(checkpoint1, checkpoint2)
}