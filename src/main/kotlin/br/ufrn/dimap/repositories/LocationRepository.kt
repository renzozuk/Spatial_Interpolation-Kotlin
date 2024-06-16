package br.ufrn.dimap.repositories

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import java.util.Collections
import java.util.stream.Collectors

class LocationRepository private constructor() {
    private val knownPoints: MutableSet<KnownPoint> = Collections.synchronizedSet(HashSet())
    private val unknownPoints: MutableSet<UnknownPoint> = Collections.synchronizedSet(HashSet())

    companion object {
        val instance: LocationRepository = LocationRepository()
    }

    val knownPointsIterator: Iterator<KnownPoint>
        get() = knownPoints.iterator()

    fun getUnknownPoints(): Set<UnknownPoint> {
        return unknownPoints.stream().collect(Collectors.toUnmodifiableSet())
    }

    val unknownPointsAsList: List<UnknownPoint>
        get() = unknownPoints.stream().toList()

    fun addKnownPoint(knownPoint: KnownPoint) {
        knownPoints.add(knownPoint)
    }

    fun addUnknownPoint(unknownPoint: UnknownPoint) {
        unknownPoints.add(unknownPoint)
    }
}