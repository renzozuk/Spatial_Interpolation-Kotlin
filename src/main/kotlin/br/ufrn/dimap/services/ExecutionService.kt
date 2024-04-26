package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.stream.Collectors

object ExecutionService {
    fun runSerial(tasks: Collection<Runnable>) {
        tasks.forEach(Consumer { obj: Runnable -> obj.run() })
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runPlatformThreads(tasks: Collection<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        for (thread in tasks.stream().map { r: Runnable -> Thread.ofPlatform().name(r.javaClass.toString().split("$").last()).start(r) }.collect(Collectors.toUnmodifiableSet())) {
            thread.priority = priority
            thread.join()
        }
    }

    @JvmOverloads
    @Throws(InterruptedException::class)
    fun runVirtualThreads(tasks: Collection<Runnable>, priority: Int = Thread.MIN_PRIORITY) {
        for (thread in tasks.stream().map { r: Runnable -> Thread.ofVirtual().name(r.javaClass.toString().split("$").last()).start(r) }.collect(Collectors.toUnmodifiableSet())) {
            thread.priority = priority
            thread.join()
        }
    }
    
    fun importUsingCoroutines() {
        val lock = ReentrantLock()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { FileManagementService.importRandomData(lock) }

            val secondTask = async(Dispatchers.Default) { FileManagementService.importUnknownLocations(lock) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun interpolateUsingCoroutines() {
        val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(0, unknownPoints.size / 4)) }

            val secondTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 4, unknownPoints.size / 2)) }

            val thirdTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 2, unknownPoints.size / 4 * 3)) }

            val fourthTask = async(Dispatchers.Default) { InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 4 * 3, unknownPoints.size)) }

            runBlocking {
                firstTask.await()
                secondTask.await()
                thirdTask.await()
                fourthTask.await()
            }
        }
    }

    fun exportUsingCoroutines() {
        val lock = ReentrantLock()

        val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(lock, unknownPoints.subList(0, unknownPoints.size / 3)) }

            val secondTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(lock, unknownPoints.subList(unknownPoints.size / 3, unknownPoints.size / 3 * 2)) }

            val thirdTask = async(Dispatchers.Default) { FileManagementService.exportInterpolations(lock, unknownPoints.subList(unknownPoints.size / 3 * 2, unknownPoints.size)) }

            runBlocking {
                firstTask.await()
                secondTask.await()
                thirdTask.await()
            }
        }
    }

    val importationTasksForSerial: Set<Runnable>
        get() {
            val importKnownPoints = Runnable {
                try {
                    FileManagementService.importRandomData()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    FileManagementService.importUnknownLocations()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return setOf(importKnownPoints, importUnknownPoints)
        }

    val importationTasksForThreads: Set<Runnable>
        get() {
            val lock = ReentrantLock()

            val importKnownPoints = Runnable {
                try {
                    FileManagementService.importRandomData(lock)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    FileManagementService.importUnknownLocations(lock)
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
            val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

            val firstTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(0, unknownPoints.size / 4))
            }

            val secondTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 4, unknownPoints.size / 2))
            }

            val thirdTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 2, unknownPoints.size / 4 * 3))
            }

            val fourthTask = Runnable {
                InterpolationService.assignTemperatureToUnknownPoints(unknownPoints.subList(unknownPoints.size / 4 * 3, unknownPoints.size))
            }

            return setOf(firstTask, secondTask, thirdTask, fourthTask)
        }

    val exportationTasksForSerial: Set<Runnable>
        get() {
            val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

            val firstTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(unknownPoints.subList(0, unknownPoints.size / 3))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val secondTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(unknownPoints.subList(unknownPoints.size / 3, unknownPoints.size / 3 * 2))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val thirdTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(unknownPoints.subList(unknownPoints.size / 3 * 2, unknownPoints.size))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return setOf(firstTask, secondTask, thirdTask)
        }

    val exportationTasksForThreads: Set<Runnable>
        get() {
            val lock = ReentrantLock()

            val unknownPoints: List<UnknownPoint> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

            val firstTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(lock, unknownPoints.subList(0, unknownPoints.size / 3))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val secondTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(lock, unknownPoints.subList(unknownPoints.size / 3, unknownPoints.size / 3 * 2))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val thirdTask = Runnable {
                try {
                    FileManagementService.exportInterpolations(lock, unknownPoints.subList(unknownPoints.size / 3 * 2, unknownPoints.size))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return setOf(firstTask, secondTask, thirdTask)
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