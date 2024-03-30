package services

import entities.Interpolation
import entities.Moment
import entities.Point
import entities.TemperatureMeasurement
import repositories.TemperatureMeasurementRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

object FileManagementService {
    private const val HOME = "src//resources//"

    @JvmStatic
    fun importDatabase(databaseName: String) {
        val path = File(HOME + "databases//" + databaseName)
        val contents = path.list()

        for(content in contents!!){
            val reader = Files.newBufferedReader(Paths.get(path.absolutePath + "//" + content))
            val lines: MutableList<String> = ArrayList()

            while(reader.ready()){
                lines.addLast(reader.readLine())
            }

            reader.close()

            if(databaseName.startsWith("Brazil")){
                readBrazilianDatabaseFile(lines)
            }

            if(databaseName.startsWith("Uruguay")){
                readUruguayanDatabaseFile(lines)
            }
        }
    }

    @JvmStatic
    fun readBrazilianDatabaseFile(lines: List<String>){
        val stateRelation = lines.get(1).split(";")
        val stationRelation = lines.get(2).split(";")
        val latitudeRelation = lines.get(4).replace(",", ".").split(";")
        val longitudeRelation = lines.get(5).replace(",", ".").split(";")

        for(line in lines.subList(9, lines.size)){
            val information = line.split(";")

            try{
                val temperature = information[7].replace(",", ".").toDouble()

                addMeasurementToRepository("${stationRelation[1]} - ${stateRelation[1]}",
                    latitudeRelation[1].toDouble(), longitudeRelation[1].toDouble(),
                    temperature, information[0], information[1].substring(0, 4))
            }catch(ignored: ArrayIndexOutOfBoundsException){

            }catch(ignored: NumberFormatException){

            }
        }
    }

    @JvmStatic
    fun readUruguayanDatabaseFile(lines: List<String>){
        for(line in lines.subList(1, lines.size)){
            val information = line.split(",")

            try{
                when(information[1]){
                    "CARRASCO" -> addMeasurementToRepository(
                        "Aeropuerto - Carrasco (Uruguay)",
                        -34.833, -56.013,
                        information[2].toDouble(),
                        Moment(information[0])
                    )

                    "aeropuertomelillag3" -> addMeasurementToRepository(
                        "Aeropuerto - Melilla (Uruguay)",
                        -34.790, -56.266,
                        information[2].toDouble(),
                        Moment(information[0])
                    )
                }
            }catch(ignored: ArrayIndexOutOfBoundsException){

            }catch(ignored: NumberFormatException){

            }
        }
    }

    @JvmStatic
    fun addMeasurementToRepository(name: String, latitude: Double, longitude: Double, temperature: Double, moment: Moment){
        val temperatureMeasurementRepository = TemperatureMeasurementRepository.getInstance()

        temperatureMeasurementRepository.addRelation(moment, TemperatureMeasurement(name, latitude, longitude, temperature))
    }

    @JvmStatic
    fun addMeasurementToRepository(name: String, latitude: Double, longitude: Double, temperature: Double, date: String, time: String){
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/ddHHmm")
        val localDateTime = LocalDateTime.parse("$date$time", dateTimeFormatter)
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Etc/GMT"))
        val moment = Moment(zonedDateTime.toInstant())

        addMeasurementToRepository(name, latitude, longitude, temperature, moment)
    }

    @JvmStatic
    fun importLocations(): List<Point> {
        val locationsPath = Path(HOME + "locations.csv")

        val locations: MutableList<Point> = ArrayList()

        if(locationsPath.exists()){
            val lines = locationsPath.readLines()

            for(line in lines){
                val information = line.split(";")

                locations.addLast(Point(information[0], information[1].toDouble(), information[2].toDouble()))
            }
        }

        return locations
    }

    @JvmStatic
    fun exportInterpolation(interpolation: Interpolation) {
        val exportationPath = Path("${HOME}output//${interpolation.mainTemperatureMeasurement.point.name}_${interpolation.mainTemperatureMeasurement.point.latitude}_${interpolation.mainTemperatureMeasurement.point.longitude}.csv")

        if(!exportationPath.exists()){
            exportationPath.createFile()
        }

        exportationPath.appendText("${interpolation.moment};${interpolation.mainTemperatureMeasurement.temperature}\n")
    }
}