@file:Suppress("NOTHING_TO_INLINE")

package wumo.world.util.math

import java.util.concurrent.ThreadLocalRandom

inline fun Rand() = ThreadLocalRandom.current()!!
fun Rand(low: Double, high: Double, n: Int) = DoubleArray(n) { Rand().nextDouble(low, high) }
operator fun DoubleArray.unaryMinus() =
    DoubleArray(this.size) {
      -this[it]
    }