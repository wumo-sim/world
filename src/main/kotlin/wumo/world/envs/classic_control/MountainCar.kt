package wumo.world.envs.classic_control

import com.badlogic.gdx.graphics.Color.BLACK
import com.badlogic.gdx.graphics.Color.YELLOW
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import wumo.sim.graphics.Config
import wumo.sim.graphics.Geom
import wumo.sim.graphics.ShapeType.Line_Strip
import wumo.sim.graphics.Viewer
import wumo.world.core.Env
import wumo.world.spaces.Box
import wumo.world.spaces.Discrete
import wumo.world.util.math.Rand
import wumo.world.util.rangeTo
import wumo.world.util.tuples.tuple4
import java.util.concurrent.CyclicBarrier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MountainCar : Env<DoubleArray, Int> {
  companion object {
    val min_position = -1.2
    val max_position = 0.6
    val max_speed = 0.07
    val goal_position = 0.5
  }
  
  val state = DoubleArray(2)
  
  override val action_space = Discrete(3)
  override val observation_space = Box(low = doubleArrayOf(min_position, -max_speed),
                                       high = doubleArrayOf(max_position, max_speed))
  
  override fun step(action: Int): tuple4<DoubleArray, Double, Boolean, Map<String, Any>> {
    assert(action_space.contains(action)) { "invalid action:$action" }
    var (position, velocity) = state
    velocity += (action - 1) * 0.001 + cos(3 * position) * (-0.0025)
    velocity = velocity.coerceIn(-max_speed, max_speed)
    position += velocity
    position = position.coerceIn(min_position, max_position)
    if (position == min_position && velocity < 0) velocity = 0.0
    val done = position >= goal_position
    val reward = -1.0
    state[0] = position
    state[1] = velocity
    return tuple4(state.clone(), reward, done, emptyMap())
  }
  
  override fun reset(): DoubleArray {
    state[0] = Rand().nextDouble(-0.6, -0.4)
    state[1] = 0.0
    return state.clone()
  }
  
  lateinit var viewer: Viewer
  lateinit var car: Geom
  var scale: Float = 1f
  fun height(x: Double) = sin(3 * x) * .45 + .55
  override fun render() {
    if (!::viewer.isInitialized) {
      val screen_width = 600
      val screen_height = 400
      val world_width = max_position - min_position
      scale = screen_width / world_width.toFloat()
      val carwidth = 40
      val carheight = 20
      viewer = Viewer(Config(screen_width, screen_height, isContinousRendering = false))
      viewer += Geom(Line_Strip) {
        color(BLACK)
        for (x in (min_position..max_position) / 100) {
          val y = height(x)
          vertex((x - min_position).toFloat() * scale, y.toFloat() * scale)
        }
      }
      val clearance = 10f
      car = Geom {
        color(BLACK)
        val l = -carwidth / 2f
        val r = carwidth / 2f
        val t = carheight.toFloat()
        val b = 0f
        rect_filled(l, b, r, b, r, t, l, t)
        color(YELLOW)
        circle(carwidth / 4f, 0f, carheight / 2.5f)
        circle(-carwidth / 4f, 0f, carheight / 2.5f)
      }.attr {
        translation.add(0f, clearance)
      }
      viewer += car
      viewer += Geom {
        val flagx = ((goal_position - min_position) * scale).toFloat()
        val flagy1 = (height(goal_position) * scale).toFloat()
        val flagy2 = flagy1 + 50
        color(BLACK)
        rectLine(flagx, flagy1, flagx, flagy2, 1f)
        color(YELLOW)
        triangle_filled(flagx, flagy2, flagx, flagy2 - 10, flagx + 25, flagy2 - 5)
      }
      viewer.startAsync()
    }
    val pos = state[0]
    car.translation.set(((pos - min_position) * scale).toFloat(), (height(pos) * scale).toFloat())
    car.rotation = cos(3 * pos).toFloat()
    viewer.requestRender()
    Thread.sleep(1000 / 60)
  }
  
  override fun close() {
    viewer.close()
  }
  
  override fun seed() {
  }
}