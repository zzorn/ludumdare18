package ludumdare18

import _root_.org.sgine.property.animate.{EasingColorAnimator}
import org.sgine.easing.Back
import org.sgine.work.Updatable
import org.sgine.property.animate.{EasingColorAnimator, EasingNumericAnimator}
import scala.math.random
import scala.math.Pi
import org.sgine.ui.BulgingBox
import org.sgine.scene.Node
import org.sgine.render.{RenderImage, TextureManager, StandardDisplay}
import org.sgine.core.{Resource, Color}

/**
 * 
 */
class Screen extends StandardDisplay with Updatable {
  private val animationTimeSeconds = 3.0;
  private var countdown = 0.0;

  var bulgy: Boxy = null

  def setup() {
    bulgy = new Boxy()
    scene += bulgy

    //bulgy.texture := RenderImage(TextureManager(Resource("noise1.png")))
    
    val easingMethod = Back.easeInOut _
    val adjustTime = animationTimeSeconds

//    bulgy.width.animator = new EasingNumericAnimator(easingMethod, adjustTime)
//    bulgy.height.animator = new EasingNumericAnimator(easingMethod, adjustTime)
//    bulgy.depth.animator = new EasingNumericAnimator(easingMethod, adjustTime)

    bulgy.rotation.x.animator = new EasingNumericAnimator(easingMethod, adjustTime)
    bulgy.rotation.y.animator = new EasingNumericAnimator(easingMethod, adjustTime)
    bulgy.rotation.z.animator = new EasingNumericAnimator(easingMethod, adjustTime)

    bulgy.color.animator = new EasingColorAnimator(easingMethod, adjustTime)
//    bulgy.centerColor.animator = new EasingColorAnimator(easingMethod, adjustTime)

//    bulgy.bulging.animator = new EasingNumericAnimator(easingMethod, adjustTime)

    initUpdatable()
  }


  override def update(time: Double) = {
    // Every animationTimeSeconds we specify new values for the box properties
    countdown -= time
    if (countdown <= 0) {
      countdown = animationTimeSeconds

      setNewBoxTargets()
    }

  }



  def setNewBoxTargets() {
    // Set new values for box size, angle and color.
    // Because we specified adjusters, the values will be animated from their current value to the new value automatically.
//    bulgy.width := random * 500
//    bulgy.height := random * 500
//    bulgy.depth := random * 500

    var Tau = 2 * Pi
    bulgy.rotation.x := random * Tau
    bulgy.rotation.y := random * Tau
    bulgy.rotation.z := random * Tau

  //  bulgy.bulging := random * 2 - 1.0

    bulgy.color := Color(random, random, random, 1)
//    bulgy.centerColor := Color(random, random, random, 1)
  }
}