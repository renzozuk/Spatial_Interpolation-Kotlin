import entities.Interpolation
import entities.Moment
import entities.TemperatureMeasurement
import repositories.TemperatureMeasurementRepository

// This main function is temporary and it's only for tests
fun main() {
    val tm1 = TemperatureMeasurement("Rio de Janeiro - RJ (Brazil)", -22.910712, -43.209781, 37.0)
    val tm2 = TemperatureMeasurement("Rawson (Argentina)", -43.300482, -65.098865, 4.0)
    val tm3 = TemperatureMeasurement("Goianinha - RN (Brazil)", -6.265929, -35.210254, 24.0)
    val tm4 = TemperatureMeasurement("Canela - RS (Brazil)", -29.364781, -50.802594, 14.5)

    println(tm1.point.getDistanceFromAnotherPoint(tm2.point))
    println(tm1.point.getDistanceFromAnotherPoint(tm3.point))
    println(tm2.point.getDistanceFromAnotherPoint(tm3.point))

    val temperatureMeasurementRepository = TemperatureMeasurementRepository.getInstance()

    var moment = Moment("2021-04-20T00:00:00.000Z")

    temperatureMeasurementRepository.addRelation(moment, tm1)
    temperatureMeasurementRepository.addRelation(moment, tm2)
    temperatureMeasurementRepository.addRelation(moment, tm4)
    temperatureMeasurementRepository.addRelation(++moment, tm2)

    println(temperatureMeasurementRepository)

    val interpolation = Interpolation(tm3.point, Moment("2021-04-20T00:00:00.000Z"))

    print(interpolation)
}