package br.ufrn.dimap.entities

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class Point(latitude: Double, longitude: Double) {
    val latitude: Double
    val longitude: Double
    private var temperature: Double? = null

    init {
        require(!(latitude < -90.0 || latitude > 90.00)) { "Invalid value for latitude. Note that the latitude value must be between -90 and 90." }

        require(!(longitude < -180.00 || longitude > 180.00)) { "Invalid value for longitude. Note that the longitude value must be between -180 and 180." }

        this.latitude = latitude
        this.longitude = longitude
    }

    constructor(latitude: Double, longitude: Double, temperature: Double?) : this(latitude, longitude) {
        this.temperature = temperature
    }

    fun getTemperature(): Double? {
        return temperature
    }

    fun setTemperature(temperature: Double?) {
        if (this is UnknownPoint) {
            this.temperature = temperature
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    fun getDistanceFromAnotherPoint(point: Point): Double {
        if (this === point) {
            return 0.0
        }

        val latitudeDistance = point.latitude * Math.PI / 180 - latitude * Math.PI / 180
        val longitudeDistance = point.longitude * Math.PI / 180 - longitude * Math.PI / 180
        val a =
            sin(latitudeDistance / 2) * sin(latitudeDistance / 2) + cos(latitude * Math.PI / 180) * cos(
                point.latitude * Math.PI / 180
            ) * sin(longitudeDistance / 2) * sin(longitudeDistance / 2)
        //        6378.137 = earth radius
        return 6378.137 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}