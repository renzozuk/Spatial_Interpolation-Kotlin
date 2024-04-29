package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportationTask
import br.ufrn.dimap.services.ExecutionService.mutexVersionOfImportationTasksForThreads as importationTasks
import br.ufrn.dimap.services.ExecutionService.interpolationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.ExecutionService.runPlatformThreads
import br.ufrn.dimap.services.FileManagementService.defineExportationPath
import java.lang.System.currentTimeMillis

@Throws(InterruptedException::class)
fun main() {
    val checkpoint1 = currentTimeMillis()

    runPlatformThreads(importationTasks)

    val checkpoint2 = currentTimeMillis()

    runPlatformThreads(interpolationTasks)

    val checkpoint3 = currentTimeMillis()

    defineExportationPath()
    runPlatformThreads(exportationTask)

    val checkpoint4 = currentTimeMillis()

    printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}