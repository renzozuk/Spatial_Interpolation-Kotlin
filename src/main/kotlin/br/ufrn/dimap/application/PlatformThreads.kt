package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportationTasksForThreads
import br.ufrn.dimap.services.ExecutionService.importationTasksForThreads
import br.ufrn.dimap.services.ExecutionService.interpolationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.ExecutionService.runPlatformThreads

@Throws(InterruptedException::class)
fun main() {
    runPlatformThreads(importationTasksForThreads)

    val checkpoint1 = System.currentTimeMillis()

    runPlatformThreads(interpolationTasks)

    val checkpoint2 = System.currentTimeMillis()

    runPlatformThreads(exportationTasksForThreads)

    printResult(checkpoint1, checkpoint2)
}