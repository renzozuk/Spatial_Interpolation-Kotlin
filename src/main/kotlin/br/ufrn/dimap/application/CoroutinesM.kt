package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.mutexVersionOfExportingUsingCoroutines as runExportationTasks
import br.ufrn.dimap.services.ExecutionService.mutexVersionOfImportationUsingCoroutines as runImportationTasks
import br.ufrn.dimap.services.ExecutionService.interpolateUsingCoroutines as runInterpolationTasks
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
    runExportationTasks()

    val checkpoint4 = currentTimeMillis()

    printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}