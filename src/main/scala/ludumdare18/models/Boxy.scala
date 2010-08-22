package ludumdare18

import scala.math.random
import org.sgine.ui.{BulgingBox, CompositeComponent, Component}
import org.sgine.core.{Resource, Color}
import org.sgine.render.{TextureManager, RenderImage}
import org.sgine.property.animate.EasingNumericAnimator
import org.sgine.easing
import org.sgine.effect.{Effect, CompositeEffect, PropertyChangeEffect, PauseEffect}

/**
 * A boxy enemy.  Basically a box of some color that can bulge out or in and that has googly eyes.
 */
class Boxy() extends CompositeComponent {

  val body = new BulgingBox(100.0, 500.0, 150.0, 0.3, 5, Color(0.1, 0.2, 0.9), Color(0.2,0.4,1.0), RenderImage(TextureManager(Resource("blurred_noise1.png"))))

  var animations: List[Effect] = Nil

  this += body

  animateIdle

  def stopAnimations() {
    animations foreach (_.stop())
    animations = Nil
  }

  def addAnimation(effect: Effect) {
    animations = effect :: animations
    effect.start()
  }

  def animateIdle {
    stopAnimations()

    body.bulging.animator = new EasingNumericAnimator(easing.Cubic.easeInOut _, 2)

    val breathe = new CompositeEffect(
      new PauseEffect(random * 1),
      new PropertyChangeEffect[Double](body.bulging, -0.1),
      new PauseEffect(random * 1),
      new PropertyChangeEffect[Double](body.bulging, 0.3))
    breathe.repeat = -1
    addAnimation(breathe)
  }

  def animateDead {
    stopAnimations()

    body.bulging.animator = new EasingNumericAnimator(easing.Cubic.easeInOut _, 1)

    val die = new CompositeEffect(
      new PropertyChangeEffect[Double](body.bulging, 0.8),
      new PropertyChangeEffect[Double](body.bulging, -0.4))
    addAnimation(die)
  }

  def animateAgitated {
    stopAnimations()

    body.bulging.animator = new EasingNumericAnimator(easing.Elastic.easeInOut _, 0.4)
    body.height.animator = new EasingNumericAnimator(easing.Quadratic.easeInOut _, 0.3)

    val breathe = new CompositeEffect(
      new PropertyChangeEffect[Double](body.bulging, -0.2),
      new PropertyChangeEffect[Double](body.bulging, 0.5))
    breathe.repeat = -1

    val wobble = new CompositeEffect(
      new PropertyChangeEffect[Double](body.height, 350),
      new PropertyChangeEffect[Double](body.height, 450))
    wobble.repeat = -1

    addAnimation(breathe)
    addAnimation(wobble)
  }




}
