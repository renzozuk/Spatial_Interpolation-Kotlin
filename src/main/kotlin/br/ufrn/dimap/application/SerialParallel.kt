package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportationTask
import br.ufrn.dimap.services.ExecutionService.importationTasksForSerial
import br.ufrn.dimap.services.ExecutionService.interpolationTasksInParallel as interpolationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.ExecutionService.runSerial
import br.ufrn.dimap.services.FileManagementService.defineExportationPath
import java.lang.System.currentTimeMillis

fun main() {
    val checkpoint1 = currentTimeMillis()

    runSerial(importationTasksForSerial)

    val checkpoint2 = currentTimeMillis()

    runSerial(interpolationTasks)

    val checkpoint3 = currentTimeMillis()

    defineExportationPath()
    runSerial(exportationTask)

    val checkpoint4 = currentTimeMillis()

    printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}