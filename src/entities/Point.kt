package entities

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

class Point(private val name: String, val latitude: Double, val longitude: Double) {
    init {
        if(latitude < -90.0 || latitude > 90.0){
            throw IllegalArgumentException("Invalid value for latitude. Note that the latitude value must be between -90 and 90.")
        }

        if(longitude < -180.00 || longitude > 180.0){
            throw IllegalArgumentException("Invalid value for longitude. Note that the longitude value must be between -180 and 180.")
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

    override fun toString(): String {
        return "[City: $name] [Latitude: $latitude] [Longitude: $longitude]"
    }

    fun getDistanceFromAnotherPoint(point: Point): Double {
        if(this == point){
            return 0.0
        }

        val latitudeDistance = point.latitude * PI / 180 - latitude * PI / 180
        val longitudeDistance = point.longitude * PI / 180 - longitude * PI / 180
        val a = sin(latitudeDistance / 2) * sin(latitudeDistance / 2) +
                cos(latitude * Math.PI / 180) * cos(point.latitude * Math.PI / 180) *
                sin(longitudeDistance / 2) * sin(longitudeDistance / 2)
//        6378.137 = earth radius
        return 6378.137 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}