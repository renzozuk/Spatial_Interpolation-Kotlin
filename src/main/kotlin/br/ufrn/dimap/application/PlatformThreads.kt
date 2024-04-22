package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService

@Throws(InterruptedException::class)
fun main() {
    val checkpoint1 = System.currentTimeMillis()

    ExecutionService.runPlatformThreads(ExecutionService.importationTasksForThreads)

    val checkpoint2 = System.currentTimeMillis()

    ExecutionService.runPlatformThreads(ExecutionService.interpolationTasks)

    val checkpoint3 = System.currentTimeMillis()

    ExecutionService.runPlatformThreads(ExecutionService.exportationTasksForThreads)

    val checkpoint4 = System.currentTimeMillis()

    ExecutionService.printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}