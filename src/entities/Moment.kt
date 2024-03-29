package entities

import java.time.Instant
import java.time.temporal.ChronoUnit

class Moment(var instant: Instant) {
    constructor(instant: String) : this(Instant.parse(instant))

    operator fun inc(): Moment {
        return Moment(this.instant.plus(1, ChronoUnit.HOURS))
    }

    operator fun dec(): Moment {
        return Moment(this.instant.minus(1, ChronoUnit.HOURS))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Moment

        return instant == other.instant
    }

    override fun hashCode(): Int {
        return instant.hashCode()
    }

    override fun toString(): String {
        return instant.toString()
    }
}