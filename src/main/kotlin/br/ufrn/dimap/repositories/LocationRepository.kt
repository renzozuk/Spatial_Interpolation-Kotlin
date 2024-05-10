package br.ufrn.dimap.repositories

import br.ufrn.dimap.entities.KnownPoint
import br.ufrn.dimap.entities.UnknownPoint
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

class LocationRepository private constructor() {
    private val knownPoints: MutableSet<KnownPoint> = HashSet()
    private val unknownPoints: MutableSet<UnknownPoint> = HashSet()

    companion object {
        val atomicInstance: AtomicReference<LocationRepository> = AtomicReference(LocationRepository())
        val instance: LocationRepository = atomicInstance.get()
    }

    val knownPointsIterator: Iterator<KnownPoint>
        get() = knownPoints.iterator()

    fun getUnknownPoints(): Set<UnknownPoint> {
        return unknownPoints.stream().collect(Collectors.toUnmodifiableSet())
    }

    val unknownPointsAsAList: List<UnknownPoint>
        get() = unknownPoints.stream().toList()

    fun addKnownPoint(knownPoint: KnownPoint) {
        knownPoints.add(knownPoint)
    }

    fun addUnknownPoint(unknownPoint: UnknownPoint) {
        unknownPoints.add(unknownPoint)
    }
}