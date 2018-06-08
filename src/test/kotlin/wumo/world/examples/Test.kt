package wumo.world.examples

import org.junit.Test
import wumo.world.envs.classic_control.*
import wumo.world.examples.algorithm.LinearTileCodingFunc
import wumo.world.examples.algorithm.SuttonTileCoding
import wumo.world.examples.algorithm.`True Online Sarsa(λ)`
import wumo.world.examples.algorithm.argmax_tie_random
import wumo.world.util.math.Rand

class Test {
  @Test
  fun `Test Mountain_Car`() {
    val env = MountainCar()
    env.reset()
    for (i in 0 until 1000) {
      env.render()
      env.step(env.action_space.sample())
    }
    env.close()
  }
  
  @Test
  fun `Test Cart Pole`() {
    val env = CartPole()
    env.reset()
    for (i in 0 until 1000) {
      env.render()
      env.step(env.action_space.sample())
    }
    env.close()
  }
  
  @Test
  fun `Test Mountain_Car True Online Sarsa`() {
    val env = MountainCar()
    val feature = SuttonTileCoding(511, 8) { s, a, tilesFunc ->
      tilesFunc(doubleArrayOf(s[0] * 8 / (MountainCar.max_position - MountainCar.min_position),
                              s[1] * 8 / (MountainCar.max_speed + MountainCar.max_speed)), intArrayOf(a))
    }
    val func = LinearTileCodingFunc(feature)
    val π = { s: DoubleArray ->
      if (Rand().nextDouble() < 0.1)
        env.action_space.sample()
      else
        argmax_tie_random(0 until env.action_space.n) { func(s, it) }
    }
    env.`True Online Sarsa(λ)`(
        Qfunc = func,
        π = π,
        λ = 0.96,
        α = 0.3 / 8,
        episodes = 9000
    )
    env.close()
  }
  
  @Test
  fun `Test Cart Pole True Online Sarsa`() {
    val env = CartPole()
    val feature = SuttonTileCoding(511, 8) { s, a, tilesFunc ->
      tilesFunc(doubleArrayOf(s[0] * 8 / (CartPole.x_threshold * 4), s[1],
                              s[2] * 8 / (CartPole.theta_threshold_radians * 4), s[3]), intArrayOf(a))
    }
    val func = LinearTileCodingFunc(feature)
    val π = { s: DoubleArray ->
      if (Rand().nextDouble() < 0.1)
        env.action_space.sample()
      else
        argmax_tie_random(0 until env.action_space.n) { func(s, it) }
    }
    env.`True Online Sarsa(λ)`(
        Qfunc = func,
        π = π,
        λ = 0.96,
        α = 0.3 / 8,
        episodes = 9000
    )
    env.close()
  }
}