package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import java.io.BufferedWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Files.newBufferedReader
import java.nio.file.Files.newBufferedWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.locks.Lock
import kotlin.io.path.*

object FileManagementService {
    private const val HOME = "src//main//resources//"

    @Throws(IOException::class)
    fun importKnownLocations(dataPath: String) {
        val locationRepository: LocationRepository = LocationRepository.instance!!

        val bufferedReader = newBufferedReader(Path.of(HOME + dataPath))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addKnownPoint(KnownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0, information?.get(2)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importKnownLocations(lock: Lock, dataPath: String) {
        lock.lock()

        val locationRepository: LocationRepository = LocationRepository.instance!!

        lock.unlock()

        val bufferedReader = newBufferedReader(Path.of(HOME + dataPath))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addKnownPoint(KnownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0, information?.get(2)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class)
    fun importRandomData() {
        importKnownLocations("databases//random_data.csv")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importRandomData(lock: Lock) {
        importKnownLocations(lock, "databases//random_data.csv")
    }

    @Throws(IOException::class)
    fun importTrueData() {
        importKnownLocations("databases//true_data.csv")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importTrueData(lock: Lock) {
        importKnownLocations(lock, "databases//true_data.csv")
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importUnknownLocations() {
        val locationRepository: LocationRepository = LocationRepository.instance!!

        val bufferedReader = newBufferedReader(Path.of(HOME + "unknown_locations.csv"))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addUnknownPoint(UnknownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun importUnknownLocations(lock: Lock) {
        lock.lock()

        val locationRepository: LocationRepository = LocationRepository.instance!!

        lock.unlock()

        val bufferedReader = newBufferedReader(Path.of(HOME + "unknown_locations.csv"))

        var line: String?

        while ((bufferedReader.readLine().also { line = it }) != null) {
            val information = line?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            locationRepository.addUnknownPoint(UnknownPoint(information?.get(0)?.toDouble() ?: 0.0, information?.get(1)?.toDouble() ?: 0.0))
        }

        bufferedReader.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(lock: Lock) {
        val filePath = Path.of(HOME + "output//exported_locations.csv")

        lock.lock()

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        lock.unlock()

        val bufferedWriter = newBufferedWriter(filePath, StandardOpenOption.APPEND)

        for (unknownPoint in LocationRepository.instance!!.getUnknownPoints()) {
            writeLine(bufferedWriter, unknownPoint)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(unknownPoints: Collection<UnknownPoint?>) {
        val filePath = Path.of(HOME + "output//exported_locations.csv")

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        val bufferedWriter = newBufferedWriter(filePath, StandardOpenOption.APPEND)

        for (unknownPoint in unknownPoints) {
            writeLine(bufferedWriter, unknownPoint!!)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(lock: Lock, unknownPoints: Collection<UnknownPoint?>) {
        val filePath = Path.of(HOME + "output//exported_locations.csv")

        lock.lock()

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        lock.unlock()

        val bufferedWriter = newBufferedWriter(filePath, StandardOpenOption.APPEND)

        for (unknownPoint in unknownPoints) {
            writeLine(bufferedWriter, unknownPoint!!)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class)
    private fun writeLine(bufferedWriter: BufferedWriter, unknownPoint: UnknownPoint) {
        bufferedWriter.write(java.lang.String.format("%.6f", unknownPoint.latitude))
        bufferedWriter.write(";")
        bufferedWriter.write(java.lang.String.format("%.6f", unknownPoint.longitude))
        bufferedWriter.write(";")
        bufferedWriter.write(java.lang.String.format("%.1f", unknownPoint.getTemperature()))
        bufferedWriter.newLine()
    }
}