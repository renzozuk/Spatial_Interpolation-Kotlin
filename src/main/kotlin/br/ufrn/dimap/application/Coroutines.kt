package br.ufrn.dimap.application

import br.ufrn.dimap.services.ExecutionService.exportUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.importUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.interpolateUsingCoroutines
import br.ufrn.dimap.services.ExecutionService.printResult
import br.ufrn.dimap.services.FileManagementService.defineExportationPath
import java.lang.System.currentTimeMillis

fun main() {
    val checkpoint1 = currentTimeMillis()

    importUsingCoroutines()

    val checkpoint2 = currentTimeMillis()

    interpolateUsingCoroutines()

    val checkpoint3 = currentTimeMillis()

    defineExportationPath()
    exportUsingCoroutines()

    val checkpoint4 = currentTimeMillis()

    printResult(checkpoint1, checkpoint2, checkpoint3, checkpoint4)
}