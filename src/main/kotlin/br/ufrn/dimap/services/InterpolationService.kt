package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.util.Math.pow
import java.util.function.Consumer

object InterpolationService {
    fun assignTemperatureToUnknownPoint(unknownPoint: UnknownPoint) {
        var numerator = 0.0
        var denominator = 0.0

        val knownPointsIterator: Iterator<KnownPoint> = LocationRepository.instance.knownPointsIterator

        while (knownPointsIterator.hasNext()) {
            val knownPoint = knownPointsIterator.next()

            // 3 is power parameter
            val distancePoweredToPowerParameter = pow(unknownPoint.getDistanceFromAnotherPoint(knownPoint), 3)

            numerator += knownPoint.getTemperature()!! / distancePoweredToPowerParameter
            denominator += 1 / distancePoweredToPowerParameter
        }

        unknownPoint.setTemperature(numerator / denominator)
    }

    fun assignTemperatureToUnknownPoints(unknownPoints: Collection<UnknownPoint?>) {
        unknownPoints.forEach(Consumer { unknownPoint: UnknownPoint? -> assignTemperatureToUnknownPoint(unknownPoint!!) })
    }
}