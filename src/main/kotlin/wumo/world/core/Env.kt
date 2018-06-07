package wumo.world.core

import wumo.world.util.tuples.tuple2
import wumo.world.util.tuples.tuple4

interface Env<O, A> {
  val reward_range: tuple2<Double, Double>
    get() = tuple2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
  val action_space: Space<A>
  val observation_space: Space<O>
  fun step(action: A): tuple4<O, Double, Boolean, Map<String, Any>>
  fun reset(): O
  fun render()
  fun close()
  fun seed()
  
}