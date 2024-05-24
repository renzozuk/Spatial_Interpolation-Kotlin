package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportUsingCoroutines as runExportationTask
import br.ufrn.dimap.services.ExecutionService.interpolateUsingCoroutinesInParallel as runInterpolationTasks
import br.ufrn.dimap.services.ExecutionService.mutexVersionOfImportationUsingCoroutines as runImportationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.FileManagementService.defineExportationPath
import java.lang.System.currentTimeMillis

fun main() {
    val checkpoint1 = currentTimeMillis()

    runImportationTasks()

    val checkpoint2 = currentTimeMillis()

    runInterpolationTasks()

    val checkpoint3 = currentTimeMillis()

    defineExportationPath()
    runExportationTask()

    val checkpoint4 = currentTimeMillis()

    printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}