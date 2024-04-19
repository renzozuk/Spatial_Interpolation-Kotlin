package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.function.Consumer

object ExecutionService {
    fun runSerial(tasks: List<Runnable>) {
        tasks.forEach(Consumer { obj: Runnable -> obj.run() })
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runPlatformThreads(tasks: List<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        val threads = tasks.stream().map { r: Runnable -> Thread.ofPlatform().name(r.javaClass.simpleName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]).start(r) }.toList()

        for (thread in threads) {
            thread.priority = priority
            thread.join()
        }
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runVirtualThreads(tasks: List<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        val threads = tasks.stream().map { r: Runnable -> Thread.ofVirtual().name(r.javaClass.simpleName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]).start(r) }.toList()

        for (thread in threads) {
            thread.priority = priority
            thread.join()
        }
    }
    
    fun importUsingCoroutines() {
        val semaphore = Semaphore(1)

        runBlocking {
            val firstTask = async(Dispatchers.Default) { FileManagementService.importRandomData(semaphore) }

            val secondTask = async(Dispatchers.Default) { FileManagementService.importUnknownLocations(semaphore) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun interpolateUsingCoroutines() {
        val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(0, unknownPoints.size / 2)) }

            val secondTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 2, unknownPoints.size)) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun exportUsingCoroutines() {
        val semaphore = Semaphore(1)

        val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(0, unknownPoints.size / 3)) }

            val secondTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(unknownPoints.size / 3, unknownPoints.size / 3 * 2)) }

            val thirdTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(unknownPoints.size / 3 * 2, unknownPoints.size)) }

            runBlocking {
                firstTask.await()
                secondTask.await()
                thirdTask.await()
            }
        }
    }

    val importationTasks: List<Runnable>
        get() {
            val semaphore = Semaphore(1)

            val importKnownPoints = Runnable {
                try {
                    FileManagementService.importRandomData(semaphore)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    FileManagementService.importUnknownLocations(semaphore)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return listOf(importKnownPoints, importUnknownPoints)
        }

    val interpolationTasks: List<Runnable>
        get() {
            val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

            val firstTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(0, unknownPoints.size / 2))
            }

            val secondTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 2, unknownPoints.size))
            }

            return listOf(firstTask, secondTask)
        }

    val exportationTasks: List<Runnable>
        get() {
            val semaphore = Semaphore(1)

            val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

            val firstTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(0, unknownPoints.size / 3))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val secondTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(unknownPoints.size / 3, unknownPoints.size / 3 * 2))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val thirdTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(semaphore, unknownPoints.subList(unknownPoints.size / 3 * 2, unknownPoints.size))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return listOf(firstTask, secondTask, thirdTask)
        }

    fun printResult(checkpoint1: Long, checkpoint2: Long, checkpoint3: Long, checkpoint4: Long) {
        println("Time to read the known and unknown locations: %.3fs".format((checkpoint2 - checkpoint1) / 1e3))
        println("Interpolation time: %.3fs".format((checkpoint3 - checkpoint2) / 1e3))
        println("Time to export the required locations: %.3fs".format((checkpoint4 - checkpoint3) / 1e3))
        println("Total time: %.3fs%n".format((checkpoint4 - checkpoint1) / 1e3))
    }
}