package services

import entities.Interpolation
import entities.Moment
import entities.Point
import util.MomentIterator

object InterpolationService {
    @JvmStatic
    fun exportInterpolation(moment: Moment, locations: List<Point>){
        for(location in locations){
            FileManagementService.exportInterpolation(Interpolation(location, moment))
        }
    }

    @JvmStatic
    fun exportInterpolation(iterator: MomentIterator, locations: List<Point>){
        while(iterator.hasNext()){
            exportInterpolation(iterator.current(), locations)
            iterator.next()
        }
    }
}