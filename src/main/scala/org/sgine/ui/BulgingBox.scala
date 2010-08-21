package org.sgine.ui

import org.sgine.core.Color
import org.sgine.render.RenderImage
import scala.math._
import simplex3d.math.doublem.Vec3d
import org.lwjgl.opengl.GL11._
import org.sgine.property.{ModifiableProperty, AdvancedProperty}
//import simplex3d.math.doublem.DoubleMath._

// NOTE: Has to be in the org.sgine.ui package, because the render method is declared as protected[ui] TODO: Change this in sgine

// TODO: Add shading?
class BulgingBox() extends Component {

  def this(width: Double,
           height: Double,
           depth: Double,
           bulging: Double = 0.0,
           subdivisions: Int = 8,
           edgeColor: Color = Color.White,
           centerColor: Color = Color.White,
           texture: RenderImage = null) {
    this()
    this.width := width
    this.height := height
    this.depth := depth
    this.bulging := bulging
    this.subdivisions := subdivisions
    this.color := edgeColor
    this.centerColor := centerColor
    this.texture := texture
  }


  /** Width of the box (size along x axis). */
  val width = new AdvancedProperty[Double](100.0, this)

  /** Height of the box (size along y axis). */
  val height = new AdvancedProperty[Double](100, this)

  /** Depth of the box (size along z axis). */
  val depth = new AdvancedProperty[Double](100, this)

  /** Amount the box bulges out (positive) or in (negative).  0 = no bulging. */
  val bulging = new AdvancedProperty[Double](0.2, this)

  val subdivisions = new AdvancedProperty[Int](8, this)

  /* Texture image of the box, or null if no texture should be used. */
  val texture = new AdvancedProperty[RenderImage](null, this)

  val centerColor = new AdvancedProperty[Color](Color.White, this) with ModifiableProperty[Color]


  protected[ui] def drawComponent() {
    if (texture() != null) texture().texture.bind()

    glBegin(GL_TRIANGLES)

    val d = depth()
    val h = height()
    val w = width()

    def mix(a: Double, b: Double) = (a + b) / 2
    
    renderSide(Vec3d(-1, -1, -1),  Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0, -1,  0), mix(w,d))
    renderSide(Vec3d( 1, -1,  1), -Vec3d.UnitZ, Vec3d.UnitY, Vec3d( 1,  0,  0), mix(h,d))
    renderSide(Vec3d( 1,  1, -1), -Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0,  1,  0), mix(w,d))
    renderSide(Vec3d(-1,  1, -1), -Vec3d.UnitY, Vec3d.UnitZ, Vec3d(-1,  0,  0), mix(h,d))
    renderSide(Vec3d( 1, -1, -1), -Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0, -1), mix(w,h))
    renderSide(Vec3d(-1, -1,  1),  Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0,  1), mix(w,h))

    glEnd()
  }

  def renderSide(start: Vec3d, uDelta: Vec3d, vDelta: Vec3d, normal: Vec3d, bulgeFactor: Double) {

    var Tau = 2 * Pi

    def cosineInterpolate(a: Double, b: Double, t: Double): Double = {
       val mu = (1.0 - cos(t * Pi)) / 2
       a * (1.0 - mu) + b * mu
    }

    def lerp(a: Double, b: Double, t: Double): Double = a * (1.0 - t) + b * t


    val scaledStart = start * 0.5
    val size = subdivisions()
    val scale = Vec3d(width()/2, height()/2, depth()/2)

    def corner(u: Int, v: Int) {
      val nu = 1.0 * u / size
      val nv = 1.0 * v / size
      val pos = scaledStart + uDelta * nu + vDelta * nv
      pos *= scale

      // Calculate bulge
      val roundDistance = 1.0 - 2 * sqrt((0.5-nu)*(0.5-nu) + (0.5-nv)*(0.5-nv))
      val squareDistance = 1.0 - 2 * max(abs(0.5 - nv), abs(0.5 - nu))
      val distance = lerp(squareDistance, roundDistance, squareDistance)

      //val bulge = cosineInterpolate(0, 1, edgeDistance)
      val bulge = sin(Tau / 4 * distance)
      pos += normal * (bulging() * bulge * bulgeFactor * 0.25)

      glTexCoord2d(nu, nv)

      val edgeColor: Color = if(color() != null) color() else Color.White
      val cColor: Color = if(centerColor() != null) centerColor() else Color.White

      def clerp(c1: Double, c2: Double): Double = c1 * (1 - distance) + c2 * distance
      glColor4d(clerp(edgeColor.red, cColor.red),
                clerp(edgeColor.green, cColor.green),
                clerp(edgeColor.blue, cColor.blue),
                clerp(edgeColor.alpha, cColor.alpha))

      glVertex3d(pos.x, pos.y, pos.z)
    }

    var v = 0
    while (v < size) {
      var u = 0
      while (u < size) {

        /*
        Divide the squares into triangles in such a pattern that there is
        always a square division going to a block corner.

        Like so:
           ____
          |\\//|
          |\\//|
          |//\\|
          |//\\|
           ~~~~

        This ensures that bulging will look smooth along all edges.
         */
        if ((u < size / 2 && v < size / 2) ||
            (u >= size / 2 && v >= size / 2)) {

          corner(u, v)
          corner(u, v + 1)
          corner(u + 1, v + 1)

          corner(u + 1, v + 1)
          corner(u + 1, v)
          corner(u, v)

        }
        else {
          corner(u, v)
          corner(u, v + 1)
          corner(u + 1, v)

          corner(u, v + 1)
          corner(u + 1, v + 1)
          corner(u + 1, v)
        }

        u += 1
      }
      v += 1
    }
  }

}