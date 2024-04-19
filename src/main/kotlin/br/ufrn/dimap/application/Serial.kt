package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService

fun main() {
    val checkpoint1 = System.currentTimeMillis()

    ExecutionService.runSerial(ExecutionService.importationTasks)

    val checkpoint2 = System.currentTimeMillis()

    ExecutionService.runSerial(ExecutionService.interpolationTasks)

    val checkpoint3 = System.currentTimeMillis()

    ExecutionService.runSerial(ExecutionService.exportationTasks)

    val checkpoint4 = System.currentTimeMillis()

    ExecutionService.printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}