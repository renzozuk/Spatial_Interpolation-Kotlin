package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportationTasksForSerial
import br.ufrn.dimap.services.ExecutionService.importationTasksForSerial
import br.ufrn.dimap.services.ExecutionService.interpolationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.ExecutionService.runSerial

fun main() {
    runSerial(importationTasksForSerial)

    val checkpoint1 = System.currentTimeMillis()

    runSerial(interpolationTasks)

    val checkpoint2 = System.currentTimeMillis()

    runSerial(exportationTasksForSerial)

    printResult(checkpoint1, checkpoint2)
}