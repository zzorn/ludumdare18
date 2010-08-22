package ludumdare18.models

import org.sgine.ui.{ZeroToOneGradient, OutlineEnvelope, Lathe}
import org.sgine.core.Color

/**
 * Ground tile
 */
class Block extends Lathe {
  
  def colorFun(u: Double, v: Double): Color = {
    // Darker lower down
    val lum = 1.0 - v
    Color(lum, lum, lum)
  }

  surface := new OutlineEnvelope(ZeroToOneGradient(0, 0.4, 7, 8, 9, 8, 6, 3, 0), ZeroToOneGradient(1))
  colorFunction := colorFun
}