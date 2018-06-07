package wumo.world.examples

import org.junit.Test
import wumo.world.envs.classic_control.MountainCar

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
}