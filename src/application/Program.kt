package application

import services.FileManagementService
import services.InterpolationService
import util.MomentIterator

fun main() {
    val checkpoint1 = System.currentTimeMillis().toDouble()

    val locations = FileManagementService.importLocations()

    FileManagementService.importDatabase("Brazil - 2021")
    FileManagementService.importDatabase("Uruguay - 2021")
    FileManagementService.importDatabase("Brazil - 2022")
    FileManagementService.importDatabase("Uruguay - 2022")
    FileManagementService.importDatabase("Brazil - 2023")
    FileManagementService.importDatabase("Uruguay - 2023")

    val checkpoint2 = System.currentTimeMillis().toDouble()

    InterpolationService.exportInterpolation(MomentIterator("2021", "2023"), locations)

    val checkpoint3 = System.currentTimeMillis().toDouble()

    println("Time to read the database: ${String.format("%.3fs", (checkpoint2 - checkpoint1) / 1e3)}")
    println("Time to export the required locations: ${String.format("%.3fs", (checkpoint3 - checkpoint2) / 1e3)}")
    println("Total time: ${String.format("%.3fs", (checkpoint3 - checkpoint1) / 1e3)}")

}