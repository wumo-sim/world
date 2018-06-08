package wumo.world.examples.algorithm

class LinearTileCodingFunc(val feature: SuttonTileCoding) {
  val w = DoubleArray(feature.numOfComponents)
  operator fun invoke(s: DoubleArray, a: Int): Double {
    return w.innerProduct(feature(s, a))
  }
}