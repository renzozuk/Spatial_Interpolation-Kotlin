package repositories

import entities.Moment
import entities.TemperatureMeasurement

class TemperatureMeasurementRepository(var momentTemperatureMeasurementRelation: Map<Moment, MutableList<TemperatureMeasurement>>) {
    companion object {
        @Volatile
        private var instance: TemperatureMeasurementRepository? = null

        fun getInstance(): TemperatureMeasurementRepository {
            if(instance == null){
                synchronized(this){
                    if(instance == null){
                        instance = TemperatureMeasurementRepository(mutableMapOf())
                    }
                }
            }

            return instance!!
        }
    }

    override fun toString(): String {
        val result: StringBuilder = StringBuilder("Measurements:")

        for(moment in momentTemperatureMeasurementRelation.keys){
            result.append("\n\n").append(moment)

            for(tm in momentTemperatureMeasurementRelation[moment]!!){
                result.append("\n").append(tm)
            }
        }

        result.append("\n")

        return result.toString()
    }

    fun addRelation(moment: Moment, temperatureMeasurement: TemperatureMeasurement){
        if(momentTemperatureMeasurementRelation.containsKey(moment)){
            momentTemperatureMeasurementRelation[moment]?.addLast(temperatureMeasurement)
        }else{
            val temperatureMeasurements = ArrayList<TemperatureMeasurement>()

            temperatureMeasurements.add(temperatureMeasurement)

            momentTemperatureMeasurementRelation += Pair(moment, temperatureMeasurements)
        }
    }
}