package wumo.world.spaces

import wumo.world.core.Space
import wumo.world.util.math.Rand

class Discrete(val n: Int) : Space<Int> {
  override fun sample() = Rand().nextInt(n)
  override fun contains(x: Int) = x in 0..(n - 1)
}