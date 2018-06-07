package wumo.world.envs.classic_control

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.BLACK
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import wumo.sim.graphics.Geom
import wumo.sim.graphics.ShapeType
import wumo.sim.graphics.ShapeType.Line_Strip
import wumo.sim.graphics.Viewer
import wumo.world.core.Env
import wumo.world.spaces.Box
import wumo.world.spaces.Discrete
import wumo.world.util.math.Rand
import wumo.world.util.rangeTo
import wumo.world.util.tuples.tuple4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val min_position = -1.2
val max_position = 0.6
val max_speed = 0.07
val goal_position = 0.5

class MountainCar : Env<DoubleArray, Int> {
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
      viewer = Viewer(screen_width, screen_height, false)
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
      }.apply {
        attr {
          translation.add(0f, clearance, 0f)
        }
      }
      viewer += car
      viewer.startAsync()
    }
    val pos = state[0]
    car.translation.set(((pos - min_position) * scale).toFloat(), (height(pos) * scale).toFloat(), 0f)
    car.rotation.setFromAxisRad(0f, 0f, 1f, cos(3 * pos).toFloat())
    viewer.requestRender()
    Thread.sleep(1000 / 60)
  }
  
  override fun close() {
    viewer.close()
  }
  
  override fun seed() {
  }
}

class MountainCarUI : Application() {
  lateinit var canvas: Canvas
  
  companion object {
    var bound = false
    var render: (Double) -> Unit = { }
    var after: () -> Unit = {}
    var width = 450.0
    var height = 300.0
  }
  
  override fun start(ps: Stage?) {
    val primaryStage = ps!!
    primaryStage.title = "Mountain Car"
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