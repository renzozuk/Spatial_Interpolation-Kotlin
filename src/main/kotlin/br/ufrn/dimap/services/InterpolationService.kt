package br.ufrn.dimap.services

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import kotlin.math.pow

object InterpolationService {
    private fun assignTemperatureToUnknownPoint(unknownPoint: UnknownPoint) {
        var numerator = 0.0
        var denominator = 0.0
        val powerParameter = 2.5

        val knownPointsIterator: Iterator<KnownPoint> = LocationRepository.instance.knownPointsIterator

        while (knownPointsIterator.hasNext()) {
            val knownPoint = knownPointsIterator.next()
            numerator += (knownPoint.getTemperature()?:0.0) / knownPoint.getDistanceFromAnotherPoint(unknownPoint).pow(powerParameter)
            denominator += 1 / knownPoint.getDistanceFromAnotherPoint(unknownPoint).pow(powerParameter)
        }

        unknownPoint.setTemperature(numerator / denominator)
    }

    fun assignTemperatureToUnknownPoints(unknownPoints: List<UnknownPoint?>) {
        unknownPoints.forEach{ up -> assignTemperatureToUnknownPoint(up!!) }
    }
}