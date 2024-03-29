package entities

class TemperatureMeasurement(val point: Point, val temperature: Double) {
    constructor(placeName: String, latitude: Double, longitude: Double, temperature: Double):
            this(Point(placeName, latitude, longitude), temperature)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemperatureMeasurement

        if (point != other.point) return false
        if (temperature != other.temperature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = point.hashCode()
        result = 31 * result + temperature.hashCode()
        return result
    }

    override fun toString(): String {
        return String.format("%s [Temperature: %.1fºC]", point, temperature)
    }
}