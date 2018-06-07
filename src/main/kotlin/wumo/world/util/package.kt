package wumo.world.util

class ClosedDoubleRange(val start: Double, val last: Double)
  : Iterable<Double> {
  var step = last - start
  
  init {
    check(start < last && step > 0)
  }
  
  override fun iterator() = object : Iterator<Double> {
    var current = start
    override fun hasNext() = current < last
    
    override fun next(): Double {
      current += step
      return minOf(last, current)
    }
  }
  
  operator fun div(n: Int): ClosedDoubleRange {
    check(n > 0)
    this.step = (last - start) / n
    return this
  }
}

operator fun Double.rangeTo(endInclusive: Double) =
    ClosedDoubleRange(this, endInclusive)