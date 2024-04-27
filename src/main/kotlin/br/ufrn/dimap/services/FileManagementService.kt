package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import java.io.BufferedWriter
import java.io.IOException
import java.lang.String.format
import java.nio.file.Files
import java.nio.file.Files.newBufferedReader
import java.nio.file.Files.newBufferedWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.locks.Lock
import kotlin.io.path.*

object FileManagementService {
    private const val HOME = "src//main//resources//"
    private var exportationPath = Path.of(HOME + "output//exported_locations.csv")

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

    @JvmStatic
    fun defineExportationPath() {
        val zdt = Instant.now().atZone(ZoneId.of("Etc/GMT+0"))
        exportationPath = Path.of(HOME + "output/exported_locations_" + format("%04d.%02d.%02d-%02d.%02d.%02d.csv", zdt.year, zdt.monthValue, zdt.dayOfMonth, zdt.hour, zdt.minute, zdt.second))
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
        if (!Files.exists(exportationPath)) {
            Files.createFile(exportationPath)
        }

        val bufferedWriter = newBufferedWriter(exportationPath, StandardOpenOption.APPEND)

        for (unknownPoint in unknownPoints) {
            writeLine(bufferedWriter, unknownPoint!!)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exportInterpolations(lock: Lock, unknownPoints: Collection<UnknownPoint?>) {
        lock.lock()

        if (!Files.exists(exportationPath)) {
            Files.createFile(exportationPath)
        }

        lock.unlock()

        val bufferedWriter = newBufferedWriter(exportationPath, StandardOpenOption.APPEND)

        for (unknownPoint in unknownPoints) {
            writeLine(bufferedWriter, unknownPoint!!)
        }

        bufferedWriter.close()
    }

    @Throws(IOException::class)
    private fun writeLine(bufferedWriter: BufferedWriter, unknownPoint: UnknownPoint) {
        bufferedWriter.write(format("%.6f;%.6f;%.1f", unknownPoint.latitude, unknownPoint.longitude, unknownPoint.getTemperature()))
        bufferedWriter.newLine()
    }
}