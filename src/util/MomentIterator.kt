package util

import entities.Moment

class MomentIterator(private var current: Moment, private val end: Moment) {
    constructor(year: String): this(Moment("$year-01-01T00:00:00.000Z"), Moment("$year-12-31T23:59:59.000Z"))

    constructor(startYear: String, endYear: String): this(Moment("$startYear-01-01T00:00:00.000Z"), Moment("$endYear-12-31T23:59:59.000Z"))

    fun hasNext(): Boolean {
        return current < end
    }

    fun current(): Moment {
        return current
    }

    fun next(): Moment {
        if(hasNext()){
            current++
        }

        return current
    }
}