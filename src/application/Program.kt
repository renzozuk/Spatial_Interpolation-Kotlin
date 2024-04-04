package application

import services.FileManagementService.importDatabase
import services.FileManagementService.importLocations
import services.InterpolationService.exportInterpolation
import util.MomentIterator

fun main() {
    val checkpoint1 = System.currentTimeMillis().toDouble()

    val locations = importLocations()

    val firstDatabase = Runnable {
        importDatabase("Brazil - 2021")
        importDatabase("Uruguay - 2021")
    }

    val secondDatabase = Runnable {
        importDatabase("Brazil - 2022")
        importDatabase("Uruguay - 2022")
    }

    val thirdDatabase = Runnable {
        importDatabase("Brazil - 2023")
        importDatabase("Uruguay - 2023")
    }

    val firstTask = Runnable {
        exportInterpolation(MomentIterator("2021", "2023"), locations.subList(0, locations.size / 3))
    }

    val secondTask = Runnable {
        exportInterpolation(MomentIterator("2021", "2023"), locations.subList(locations.size / 3, locations.size / 3 * 2))
    }

    val thirdTask = Runnable {
        exportInterpolation(MomentIterator("2021", "2023"), locations.subList(locations.size / 3 * 2, locations.size))
    }

    runPlatformThreads(listOf(firstDatabase, secondDatabase, thirdDatabase))

    val checkpoint2 = System.currentTimeMillis().toDouble()

    runPlatformThreads(listOf(firstTask, secondTask, thirdTask))

    val checkpoint3 = System.currentTimeMillis().toDouble()

    println("Time to read the database: ${String.format("%.3fs", (checkpoint2 - checkpoint1) / 1e3)}")
    println("Time to export the required locations: ${String.format("%.3fs", (checkpoint3 - checkpoint2) / 1e3)}")
    println("Total time: ${String.format("%.3fs", (checkpoint3 - checkpoint1) / 1e3)}")
}

private fun runPlatformThreads(runnables: List<Runnable>) {
    runPlatformThreads(runnables, 1)
}

private fun runPlatformThreads(runnables: List<Runnable>, priority: Int) {
    val threads = mutableListOf<Thread>()

    for(i in runnables.indices){
        val builder = Thread.ofPlatform().name("worker-", i.toLong()).priority(priority)

        threads.addFirst(builder.start(runnables[i]))
    }

    for(thread in threads){
        thread.join()
    }
}