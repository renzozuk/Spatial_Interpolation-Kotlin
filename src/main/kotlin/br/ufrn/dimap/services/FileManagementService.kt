package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import java.io.BufferedWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.Semaphore
import kotlin.io.path.*

object FileManagementService {
    private const val HOME = "src//main//resources//"

    @Throws(IOException::class, InterruptedException::class)
    fun importKnownLocations(semaphore: Semaphore, dataPath: String) {
        semaphore.acquire()

        val locationRepository: LocationRepository = LocationRepository.instance!!

        semaphore.release()

        val bufferedReader = Files.newBufferedReader(Path.of(HOME + dataPath))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addKnownPoint(KnownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0, information?.get(2)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importRandomData(semaphore: Semaphore) {
        importKnownLocations(semaphore, "databases//random_data.csv")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importTrueData(semaphore: Semaphore) {
        importKnownLocations(semaphore, "databases//true_data.csv")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importUnknownLocations(semaphore: Semaphore) {
        semaphore.acquire()

        val locationRepository: LocationRepository = LocationRepository.instance!!

        semaphore.release()

        val bufferedReader = Files.newBufferedReader(Path.of(HOME + "unknown_locations.csv"))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addUnknownPoint(UnknownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(semaphore: Semaphore, unknownPoints: List<UnknownPoint?>) {
        val filePath = Path.of(HOME + "output//exported_locations.csv")

        semaphore.acquire()

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        semaphore.release()

        val bufferedWriter = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)

        for (unknownPoint in unknownPoints) {
            writeLine(bufferedWriter, unknownPoint!!)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(semaphore: Semaphore) {
        val filePath = Path.of(HOME + "output//exported_locations.csv")

        semaphore.acquire()

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        semaphore.release()

        val bufferedWriter = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)

        for (unknownPoint in LocationRepository.instance!!.getUnknownPoints()) {
            writeLine(bufferedWriter, unknownPoint)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class)
    private fun writeLine(bufferedWriter: BufferedWriter, unknownPoint: UnknownPoint) {
        bufferedWriter.write(java.lang.String.format("%.4f", unknownPoint.latitude))
        bufferedWriter.write(";")
        bufferedWriter.write(java.lang.String.format("%.4f", unknownPoint.longitude))
        bufferedWriter.write(";")
        bufferedWriter.write(java.lang.String.format("%.1f", unknownPoint.getTemperature()))
        bufferedWriter.newLine()
    }
}