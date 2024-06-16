package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.services.FileManagementService.exportInterpolations
import br.ufrn.dimap.services.FileManagementService.getExportationCallable
import br.ufrn.dimap.services.FileManagementService.importRandomData
import br.ufrn.dimap.services.FileManagementService.importUnknownLocations
import br.ufrn.dimap.services.InterpolationService.assignTemperatureToUnknownPoints
import br.ufrn.dimap.services.InterpolationService.assignTemperatureToUnknownPointsInParallel
import br.ufrn.dimap.services.InterpolationService.getInterpolationCallable
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.math.floor
import kotlinx.coroutines.sync.Mutex as KotlinMutex
import kotlinx.coroutines.sync.Semaphore as KotlinSemaphore
import java.util.concurrent.Semaphore as JavaSemaphore

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

    fun runPlatformThreadsUsingExecutor(task: Runnable) {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).use { executorService ->
            executorService.submit(task)
        }
    }

    fun runPlatformThreadsUsingExecutor(tasks: Collection<Runnable>) {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).use { executorService ->
            for (task in tasks) {
                executorService.submit(task)
            }
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

    fun runVirtualThreadsUsingExecutor(task: java.lang.Runnable?) {
        Executors.newVirtualThreadPerTaskExecutor().use { executorService ->
            executorService.submit(task)
        }
    }

    fun runVirtualThreadsUsingExecutor(tasks: Collection<java.lang.Runnable?>) {
        Executors.newVirtualThreadPerTaskExecutor().use { executorService ->
            for (task in tasks) {
                executorService.submit(task)
            }
        }
    }

    val importationTasksForSerial: Set<Runnable>
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

    val mutexVersionOfImportationTasksForThreads: Set<Runnable>
        get() {
            val lock = ReentrantLock()

            val importKnownPoints = Runnable {
                try {
                    importRandomData(lock)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    importUnknownLocations(lock)
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
                    importRandomData(semaphore)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            val importUnknownPoints = Runnable {
                try {
                    importUnknownLocations(semaphore)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }

            return setOf(importKnownPoints, importUnknownPoints)
        }

    fun mutexVersionOfImportationUsingCoroutines() {
        val lock = KotlinMutex()

        runBlocking {
            val firstTask = async(Dispatchers.Default) { importRandomData(lock) }

            val secondTask = async(Dispatchers.Default) { importUnknownLocations(lock) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun semaphoreVersionOfImportationUsingCoroutines() {
        val semaphore = KotlinSemaphore(1)

        runBlocking {
            val firstTask = async(Dispatchers.Default) { importRandomData(semaphore) }

            val secondTask = async(Dispatchers.Default) { importUnknownLocations(semaphore) }

            runBlocking {
                firstTask.await()
                secondTask.await()
            }
        }
    }

    fun importThroughSingleThreadAndFuture(): Set<Future<String>> {
        Executors.newSingleThreadExecutor().use { executionService ->
            val futures: MutableSet<Future<String>> = HashSet()
            futures.add(executionService.submit<String> {
                try {
                    importRandomData()

                    return@submit "Known points imported successfully."
                } catch (e: IOException) {
                    throw java.lang.RuntimeException(e)
                }
            })

            futures.add(executionService.submit<String> {
                try {
                    importUnknownLocations()

                    return@submit "Unknown points imported successfully."
                } catch (e: IOException) {
                    throw java.lang.RuntimeException(e)
                }
            })
            return futures
        }
    }

    val interpolationTasks: Set<Runnable>
        get() {
            return getInterpolationTasksByQuantity(Runtime.getRuntime().availableProcessors())
        }

    private fun getInterpolationTasksByQuantity(quantity: Int): Set<Runnable> {
        val unknownPoints: List<UnknownPoint?> = LocationRepository.instance.unknownPointsAsList

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

    val interpolationTasksInParallel: Set<Runnable>
        get() {
            return getInterpolationTasksInParallelByQuantity(Runtime.getRuntime().availableProcessors())
        }

    private fun getInterpolationTasksInParallelByQuantity(quantity: Int): Set<Runnable> {
        val unknownPoints: List<UnknownPoint?> = LocationRepository.instance.unknownPointsAsList

        val tasks: MutableSet<Runnable> = HashSet()

        IntStream.range(0, quantity).forEach { i: Int ->
            tasks.add(Runnable {
                assignTemperatureToUnknownPointsInParallel(
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
        val unknownPoints: List<UnknownPoint> = LocationRepository.instance.unknownPointsAsList

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

    fun interpolateUsingCoroutinesInParallel() {
        val unknownPoints: List<UnknownPoint> = LocationRepository.instance.unknownPointsAsList

        val quantity = Runtime.getRuntime().availableProcessors()

        runBlocking {
            val tasks: MutableSet<Deferred<*>> = HashSet()

            for (i in 0..< quantity) {
                tasks.add(async(Dispatchers.Default) { assignTemperatureToUnknownPointsInParallel(unknownPoints.subList((floor((unknownPoints.size / quantity * i).toDouble())).toInt(),
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

    fun interpolateThroughPlatformThreadsAndFuture(): Set<Future<UnknownPoint>> {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).use { executionService ->
            val futures: MutableSet<Future<UnknownPoint>> = HashSet()
            for (unknownPoint in LocationRepository.instance.getUnknownPoints()) {
                futures.add(executionService.submit(getInterpolationCallable(unknownPoint)))
            }
            return futures
        }
    }

    fun interpolateThroughVirtualThreadsAndFuture(): Set<Future<UnknownPoint>> {
        Executors.newVirtualThreadPerTaskExecutor().use { executionService ->
            val futureResult: MutableSet<Future<UnknownPoint>> = HashSet()
            for (unknownPoint in LocationRepository.instance.getUnknownPoints()) {
                futureResult.add(executionService.submit(getInterpolationCallable(unknownPoint)))
            }
            return futureResult
        }
    }

    fun runInterpolationAction() {
        val interpolationAction: InterpolationAction = InterpolationAction()
        interpolationAction.compute()
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

    fun exportThroughSingleThreadAndFuture(): Set<Future<UnknownPoint?>> {
        Executors.newSingleThreadExecutor().use { executionService ->
            val futures: MutableSet<Future<UnknownPoint?>> = HashSet()
            for (unknownPoint in LocationRepository.instance.getUnknownPoints()) {
                futures.add(executionService.submit(getExportationCallable(unknownPoint)))
            }
            return futures
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