package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportationTasksForThreads
import br.ufrn.dimap.services.ExecutionService.importationTasksForThreads
import br.ufrn.dimap.services.ExecutionService.interpolationTasks
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.ExecutionService.runVirtualThreads

@Throws(InterruptedException::class)
fun main() {
    runVirtualThreads(importationTasksForThreads)

    val checkpoint1 = System.currentTimeMillis()

    runVirtualThreads(interpolationTasks)

    val checkpoint2 = System.currentTimeMillis()

    runVirtualThreads(exportationTasksForThreads)

    printResult(checkpoint1, checkpoint2)
}