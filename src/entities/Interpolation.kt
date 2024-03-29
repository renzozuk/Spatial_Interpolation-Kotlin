package entities

import repositories.TemperatureMeasurementRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow

class Interpolation(mainPlace: Point, private var moment: Moment) {
    private var mainTemperatureMeasurement: TemperatureMeasurement
    private var placeDistanceRelation: Map<TemperatureMeasurement, Double> = HashMap()

    init {
        val temperatureMeasurementRepository: TemperatureMeasurementRepository = TemperatureMeasurementRepository.getInstance()

        placeDistanceRelation = temperatureMeasurementRepository.momentTemperatureMeasurementRelation[moment]!!.associateBy({ it }, { it.point.getDistanceFromAnotherPoint(mainPlace) })

        mainTemperatureMeasurement = TemperatureMeasurement(mainPlace, calculateTemperatureForMainPlace())
    }

    override fun toString(): String {
        val result: StringBuilder = StringBuilder("Interpolation:\n")

        result.append(mainTemperatureMeasurement.toString())

        result.append(" on ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Etc/GMT")).format(moment.instant))
            .append(" at ").append(DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Etc/GMT")).format(moment.instant))
            .append("\n")

        for(tm in placeDistanceRelation.toList().sortedBy { (_, value) -> value }.toMap().keys){
            result.append(tm).append(" [Distance: ").append(String.format("%.2fkm", placeDistanceRelation[tm])).append("]\n")
        }

        result.append("\n")

        return result.toString()
    }

    private fun calculateTemperatureForMainPlace(): Double {
        var numerator: Double = 0.0
        var denominator: Double = 0.0
        val powerParameter: Double = 2.5

        for(tm in placeDistanceRelation.keys){
            numerator += tm.temperature / placeDistanceRelation[tm]!!.pow(powerParameter)
            denominator += 1 / placeDistanceRelation[tm]!!.pow(powerParameter)
        }

        return numerator / denominator
    }
}