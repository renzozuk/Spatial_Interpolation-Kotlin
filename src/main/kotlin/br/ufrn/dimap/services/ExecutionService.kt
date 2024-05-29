package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.services.FileManagementService.exportInterpolations
import br.ufrn.dimap.services.FileManagementService.importRandomData
import br.ufrn.dimap.services.FileManagementService.importUnknownLocations
import br.ufrn.dimap.services.InterpolationService.assignTemperatureToUnknownPoints
import kotlinx.coroutines.*
import java.io.IOException
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.math.floor

object ExecutionService {
    fun runSerial(task: Runnable) {
        task.run()
    }

    fun runSerial(tasks: Collection<Runnable>) {
        tasks.forEach(Consumer { t: Runnable -> t.run() })
    }

    @Throws(InterruptedException::class)
    fun runPlatformThreads(task: Runnable) {
        val uniqueThread = Thread.ofPlatform().name(task.javaClass.toString().split("$").last()).start(task)
        uniqueThread.join()
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runPlatformThreads(tasks: Collection<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        for (thread in tasks.stream().map { r: Runnable -> Thread.ofPlatform().name(r.javaClass.toString().split("$").last()).start(r) }.collect(Collectors.toUnmodifiableSet())) {
            thread.priority = priority
            thread.join()
        }
    }

    @Throws(InterruptedException::class)
    fun runVirtualThreads(task: Runnable) {
        val uniqueThread = Thread.ofVirtual().name(task.javaClass.toString().split("$").last()).start(task)
        uniqueThread.join()
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runVirtualThreads(tasks: Collection<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        for (thread in tasks.stream().map { r: Runnable -> Thread.ofVirtual().name(r.javaClass.toString().split("$").last()).start(r) }.collect(Collectors.toUnmodifiableSet())) {
            thread.priority = priority
            thread.join()
        }
    }
    
    fun atomicVersionOfImportationUsingCoroutines() {
        runBlocking {
            val firstTask = async(Dispatchers.Default) { importRandomData() }

            val secondTask = async(Dispatchers.Default) { importUnknownLocations() }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    val atomicVersionOfImportationTasksForThreads: Set<Runnable>
        get() {
            val importKnownPoints = Runnable {
                try {
                    importRandomData()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    importUnknownLocations()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return setOf(importKnownPoints, importUnknownPoints)
        }

    val interpolationTasks: Set<Runnable>
        get() {
            return getInterpolationTasksByQuantity(Runtime.getRuntime().availableProcessors())
        }

    private fun getInterpolationTasksByQuantity(quantity: Int): Set<Runnable> {
        val unknownPoints: List<UnknownPoint?> = LocationRepository.instance.unknownPointsAsAList

        val tasks: MutableSet<Runnable> = HashSet()

        IntStream.range(0, quantity).forEach { i: Int ->
            tasks.add(Runnable {
                assignTemperatureToUnknownPoints(
                    unknownPoints.subList(
                        floor((unknownPoints.size / quantity * i).toDouble()).toInt(),
                        floor((unknownPoints.size / quantity * (i + 1)).toDouble()).toInt()
                    )
                )
            })
        }

        return tasks
    }

    fun interpolateUsingCoroutines() {
        val unknownPoints: List<UnknownPoint> = LocationRepository.instance.unknownPointsAsAList

        val quantity = Runtime.getRuntime().availableProcessors()

        runBlocking {
            val tasks: MutableSet<Deferred<*>> = HashSet()

            for (i in 0..< quantity) {
                tasks.add(async(Dispatchers.Default) { assignTemperatureToUnknownPoints(unknownPoints.subList((floor((unknownPoints.size / quantity * i).toDouble())).toInt(),
                    floor((unknownPoints.size / quantity * (i + 1)).toDouble()).toInt()
                )) })
            }

            runBlocking {
                for (task in tasks) {
                    task.await()
                }
            }
        }
    }

    val exportationTask: Runnable
        get() {
            return Runnable {
                try {
                    exportInterpolations(LocationRepository.instance.getUnknownPoints())
                } catch (e: IOException) {
                    throw java.lang.RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw java.lang.RuntimeException(e)
                }
            }
        }

    fun exportUsingCoroutines() {
        runBlocking {
            val uniqueTask = async(Dispatchers.Default) { exportInterpolations(LocationRepository.instance.getUnknownPoints()) }

            runBlocking {
                uniqueTask.await()
            }
        }
    }

    fun printResult(checkpoint1: Long, checkpoint2: Long) {
        println("Interpolation time: %.3fs".format((checkpoint2 - checkpoint1) / 1e3))
    }

    fun printResult(checkpoint1: Long, checkpoint2: Long, checkpoint3: Long, checkpoint4: Long) {
        println("Time to read the known and unknown locations: %.3fs".format((checkpoint2 - checkpoint1) / 1e3))
        printResult(checkpoint2, checkpoint3)
        println("Time to export the required locations: %.3fs".format((checkpoint4 - checkpoint3) / 1e3))
        println("Total time: %.3fs%n".format((checkpoint4 - checkpoint1) / 1e3))
    }
}