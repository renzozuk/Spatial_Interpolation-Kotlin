package br.ufrn.dimap.util

object Math {
    fun pow(x: Double, y: Int): Double {
        if (y < 0) {
            return 1 / pow(x, -y)
        }

        var result = 1.0

        for (i in 0..< y) {
            result *= x
        }

        return result
    }
}