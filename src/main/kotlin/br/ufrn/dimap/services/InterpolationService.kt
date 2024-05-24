package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.util.Math.pow
import java.util.concurrent.Callable
import java.util.function.Consumer

object InterpolationService {
    fun assignTemperatureToUnknownPoint(unknownPoint: UnknownPoint) {
        var numerator = 0.0
        var denominator = 0.0

        val knownPointsIterator: Iterator<KnownPoint> = LocationRepository.instance.knownPointsIterator

        while (knownPointsIterator.hasNext()) {
            val knownPoint = knownPointsIterator.next()

            // 3 is power parameter
            val dpp: Double = pow(unknownPoint.getDistanceFromAnotherPoint(knownPoint), 3)

            numerator += knownPoint.getTemperature()!! / dpp
            denominator += 1 / dpp
        }

        unknownPoint.setTemperature(numerator / denominator)
    }

    fun assignTemperatureToUnknownPoints(unknownPoints: Collection<UnknownPoint?>) {
        unknownPoints.forEach(Consumer { unknownPoint: UnknownPoint? -> assignTemperatureToUnknownPoint(unknownPoint!!) })
    }

    fun assignTemperatureToUnknownPointsInParallel(unknownPoints: Collection<UnknownPoint?>) {
        unknownPoints.parallelStream().forEach { unknownPoint: UnknownPoint? -> assignTemperatureToUnknownPoint(unknownPoint!!) }
    }

    fun getInterpolationCallable(unknownPoint: UnknownPoint): Callable<UnknownPoint> {
        return Callable<UnknownPoint> {
            var numerator = 0.0
            var denominator = 0.0

            val knownPointsIterator: Iterator<KnownPoint> = LocationRepository.instance.knownPointsIterator

            while (knownPointsIterator.hasNext()) {
                val knownPoint = knownPointsIterator.next()

                // 3 is power parameter
                val dpp = pow(unknownPoint.getDistanceFromAnotherPoint(knownPoint), 3)

                numerator += knownPoint.getTemperature()!! / dpp
                denominator += 1 / dpp
            }

            unknownPoint.setTemperature(numerator / denominator)
            unknownPoint
        }
    }
}