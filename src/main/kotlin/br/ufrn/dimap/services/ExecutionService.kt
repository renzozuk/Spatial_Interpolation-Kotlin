package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.services.InterpolationService.assignTemperatureToUnknownPoints
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlinx.coroutines.sync.Mutex as KotlinMutex
import kotlinx.coroutines.sync.Semaphore as KotlinSemaphore
import java.util.concurrent.Semaphore as JavaSemaphore

object ExecutionService {
    fun runSerial(tasks: Collection<Runnable>) {
        tasks.forEach(Consumer { t: Runnable -> t.run() })
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
    
    fun mutexVersionOfImportationUsingCoroutines() {
        val lock = KotlinMutex()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { FileManagementService.importRandomData(lock) }

            val secondTask = async(Dispatchers.Default) { FileManagementService.importUnknownLocations(lock) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun semaphoreVersionOfImportationUsingCoroutines() {
        val semaphore = KotlinSemaphore(1)

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

    fun mutexVersionOfExportingUsingCoroutines() {
        val lock = KotlinMutex()

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

    fun semaphoreVersionOfExportingUsingCoroutines() {
        val semaphore = KotlinSemaphore(1)

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

    val mutexVersionOfImportationTasksForThreads: Set<Runnable>
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

    val semaphoreVersionOfImportationTasksForThreads: Set<Runnable>
        get() {
            val semaphore = JavaSemaphore(1)

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

            return setOf(importKnownPoints, importUnknownPoints)
        }

    val interpolationTasks: Set<Runnable>
        get() {
            return getInterpolationTasksByQuantity(Runtime.getRuntime().availableProcessors())
        }

    private fun getInterpolationTasksByQuantity(quantity: Int): Set<Runnable> {
        val unknownPoints: List<UnknownPoint?> = LocationRepository.instance!!.getUnknownPoints().stream().toList()

        val tasks: MutableSet<Runnable> = HashSet()

        IntStream.range(0, quantity).forEach { i: Int ->
            tasks.add(Runnable {
                assignTemperatureToUnknownPoints(
                    unknownPoints.subList(
                        unknownPoints.size / quantity * i,
                        unknownPoints.size / quantity * (i + 1)
                    )
                )
            })
        }

        return tasks
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

    val mutexVersionOfExportationTasksForThreads: Set<Runnable>
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

    val semaphoreVersionOfExportationTasksForThreads: Set<Runnable>
        get() {
            val semaphore = JavaSemaphore(1)

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