@file:Suppress("NOTHING_TO_INLINE")

package wumo.world.examples.algorithm

import wumo.world.core.Env
import wumo.world.util.math.Rand

fun argmax_tie_random(set: IntRange, evaluate: (Int) -> Double): Int {
  val iterator = set.iterator()
  val max_a = mutableListOf(iterator.next())
  var max = evaluate(max_a[0])
  while (iterator.hasNext()) {
    val tmp = iterator.next()
    val p = evaluate(tmp)
    if (p > max) {
      max = p
      max_a.apply {
        clear()
        add(tmp)
      }
    } else if (p == max)
      max_a.add(tmp)
  }
  return max_a[Rand().nextInt(max_a.size)]
}

inline fun HashMap<Int, Double>.scale(s: Double) {
  for (key in keys)
    compute(key) { _, v ->
      v!! * s
    }
}

inline fun HashMap<Int, Double>.scaleAdd(s: Double, x: IntArray) {
  for (i in x) {
    compute(i) { _, v ->
      (v ?: 0.0) + s
    }
  }
}

inline fun HashMap<Int, Double>.innerProduct(x: IntArray): Double {
  var sum = 0.0
  for (i in x)
    sum += this[i] ?: 0.0
  return sum
}

inline fun DoubleArray.innerProduct(x: IntArray): Double {
  var sum = 0.0
  for (i in x)
    sum += this[i]
  return sum
}

inline fun DoubleArray.scaleAdd(s: Double, z: HashMap<Int, Double>) {
  for ((i, v) in z.entries)
    this[i] += s * v
}

inline fun DoubleArray.scaleAdd(s: Double, x: IntArray) {
  for (i in x)
    this[i] += s
}

inline fun Env<DoubleArray, Int>.`True Online Sarsa(λ)`(
    Qfunc: LinearTileCodingFunc,
    π: (DoubleArray) -> Int,
    λ: Double,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE) {
  val γ = 1.0
  val X = Qfunc.feature
  val w = Qfunc.w
  val z = HashMap<Int, Double>()
  for (episode in 0 until episodes) {
    println("$episode/$episodes")
    var step = 0
    val s = reset()
    var a = π(s)
    var x = X(s, a)
    z.clear()
    var Q_old = 0.0
    var terminal = false
    while (true) {
      step++
      if (terminal || step >= maxStep) break
      render()
      val (s_next, reward, done) = step(a)
      terminal = done
      val tmp1 = (1.0 - α * γ * λ * z.innerProduct(x))
      z.scale(γ * λ)
      z.scaleAdd(tmp1, x)
      
      val Q = w.innerProduct(x)
      var δ = reward - Q
      if (!terminal) {
        val a_next = π(s_next)
        val `x'` = X(s_next, a_next)
        val `Q'` = w.innerProduct(`x'`)
        δ += γ * `Q'`
        w.scaleAdd(α * (δ + Q - Q_old), z)
        w.scaleAdd(-α * (Q - Q_old), x)
        Q_old = `Q'`
        x = `x'`
        a = a_next
      } else {
        w.scaleAdd(α * (δ + Q - Q_old), z)
        w.scaleAdd(-α * (Q - Q_old), x)
      }
    }
  }
}