package br.ufrn.dimap.repositories

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint

class LocationRepository private constructor() {
    private val knownPoints: MutableSet<KnownPoint> = HashSet()
    private val unknownPoints: MutableSet<UnknownPoint> = HashSet()

    companion object {
        var instance: LocationRepository? = null
            get() {
                if (field == null) {
                    field = LocationRepository()
                }

                return field
            }
            private set
    }

    fun getKnownPoints(): Set<KnownPoint> {
        return knownPoints
    }

    fun getUnknownPoints(): Set<UnknownPoint> {
        return unknownPoints
    }

    fun addKnownPoint(knownPoint: KnownPoint) {
        knownPoints.add(knownPoint)
    }

    fun addUnknownPoint(unknownPoint: UnknownPoint) {
        unknownPoints.add(unknownPoint)
    }
}