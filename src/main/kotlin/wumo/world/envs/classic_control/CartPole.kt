package wumo.world.envs.classic_control

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import wumo.world.core.Env
import wumo.world.spaces.Box
import wumo.world.spaces.Discrete
import wumo.world.util.collections.arrayCopy
import wumo.world.util.math.Rand
import wumo.world.util.math.unaryMinus
import wumo.world.util.tuples.tuple4
import java.util.concurrent.CyclicBarrier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CartPole : Env<DoubleArray, Int> {
  companion object {
    val gravity = 9.8
    val masscart = 1.0
    val masspole = 0.1
    val total_mass = (masspole + masscart)
    val length = 0.5 // actually half the pole's length
    val polemass_length = (masspole * length)
    val force_mag = 10.0
    val tau = 0.02  // seconds between state updates
    
    // Angle at which to fail the episode
    val theta_threshold_radians = 12 * 2 * PI / 360
    val x_threshold = 2.4
  }
  
  // Angle limit set to 2 * theta_threshold_radians so failing observation is still within bounds
  val high = doubleArrayOf(
      x_threshold * 2,
      Double.MAX_VALUE,
      theta_threshold_radians * 2,
      Double.MAX_VALUE)
  
  val state = DoubleArray(4)
  
  var steps_beyond_done = Double.NaN
  
  override val action_space = Discrete(2)
  override val observation_space = Box(-high, high)
  
  override fun step(action: Int): tuple4<DoubleArray, Double, Boolean, Map<String, Any>> {
    assert(action_space.contains(action)) { "invalid action:$action" }
    var (x, x_dot, theta, theta_dot) = state
    
    val force = if (action == 1) force_mag else -force_mag
    val costheta = cos(theta)
    val sintheta = sin(theta)
    val temp = (force + polemass_length * theta_dot * theta_dot * sintheta) / total_mass
    val thetaacc = (gravity * sintheta - costheta * temp) / (length * (4.0 / 3.0 - masspole * costheta * costheta / total_mass))
    val xacc = temp - polemass_length * thetaacc * costheta / total_mass
    x += tau * x_dot
    x_dot += tau * xacc
    theta += tau * theta_dot
    theta_dot += tau * thetaacc
    state[0] = x
    state[1] = x_dot
    state[2] = theta
    state[3] = theta_dot
    val done = x < -x_threshold
               || x > x_threshold
               || theta < -theta_threshold_radians
               || theta > theta_threshold_radians
    val reward = when {
      !done -> 1.0
      steps_beyond_done.isNaN() -> {
        steps_beyond_done = 0.0
        1.0
      }
      else -> {
        if (steps_beyond_done == 0.0)
          println("You are calling 'step()' even though this environment has already returned done = True. You should always call 'reset()' once you receive 'done = True' -- any further steps are undefined behavior.")
        steps_beyond_done += 1
        0.0
      }
    }
    return tuple4(state, reward, done, emptyMap())
  }
  
  override fun reset(): DoubleArray {
    val s = Rand(-0.05, 0.05, 4)
    arrayCopy(s, state, state.size)
    steps_beyond_done = Double.NaN
    return s
  }
  
  override fun render() {
    if (!CartPoleUI.bound) {
    
    }
  }
  
  override fun close() {
    Platform.exit()
    CartPoleUI.bound = false
  }
  
  override fun seed() {
  }
}


class CartPoleUI : Application() {
  lateinit var canvas: Canvas
  
  companion object {
    var bound = false
    var render: (Double) -> Unit = { }
    var after: () -> Unit = {}
    var width = 600.0
    var height = 400.0
  }
  
  override fun start(ps: Stage?) {
    val primaryStage = ps!!
    primaryStage.title = "CartPole"
    val root = Group()
    canvas = Canvas(width, height)
    root.children.add(canvas)
    primaryStage.scene = Scene(root)
    primaryStage.show()
    render = this::render
    after()
  }
  
  val barrier = CyclicBarrier(2)
  fun tx(x: Double) = (x + PI / 2) / (2 * PI / 3) * width
  fun ty(y: Double) = (-y + 1) / 2 * height
  fun render(position: Double) {
    barrier.reset()
    Platform.runLater {
      val gc = canvas.graphicsContext2D
      gc.clearRect(0.0, 0.0, width, height)
      for (i in 0..40) {
        val x1 = i / 40.0 * 2 * PI / 3
        val y1 = sin(3 * (x1 + PI / 6))
        val x2 = (i + 1) / 40.0 * 2 * PI / 3
        val y2 = sin(3 * (x2 + PI / 6))
        gc.strokeLine(i / 40.0 * width, ty(y1), (i + 1) / 40.0 * width, ty(y2))
      }
      val min_x = tx(min_position)
      val min_y = ty(sin(3 * min_position))
      gc.strokeLine(min_x, min_y, min_x + 10, min_y)
      val ball_x = tx(position)
      val ball_y = ty(sin(3 * position))
      gc.strokeOval(ball_x, ball_y, 10.0, 10.0)
      barrier.await()
    }
    Thread.sleep(30)
    barrier.await()
  }
}