package org.sgine.ui

import org.sgine.core.Color
import org.sgine.property.AdvancedProperty
import org.sgine.render.RenderImage
import org.sgine.ui.Component
import scala.math._
import simplex3d.math.doublem.Vec3d
import org.lwjgl.opengl.GL11._


// NOTE: Has to be in the org.sgine.ui package, because the render method is declared as protected[ui] TODO: Change this in sgine
class BulgingBox() extends Component {

  def this(width: Double,
           height: Double,
           depth: Double,
           bulging: Double = 0.0,
           subdivisions: Int = 8,
           color: Color = Color.White,
           texture: RenderImage = null) {
    this()
    this.width := width
    this.height := height
    this.depth := depth
    this.color := color
    this.texture := texture
  }


  /** Width of the box (size along x axis). */
  val width = new AdvancedProperty[Double](100.0, this)

  /** Height of the box (size along y axis). */
  val height = new AdvancedProperty[Double](100, this)

  /** Depth of the box (size along z axis). */
  val depth = new AdvancedProperty[Double](100, this)

  /** Amount the box bulges out (positive) or in (negative).  0 = no bulging. */
  val bulging = new AdvancedProperty[Double](1, this)

  val subdivisions = new AdvancedProperty[Int](8, this)

  /* Texture image of the box, or null if no texture should be used. */
  val texture = new AdvancedProperty[RenderImage](null, this)


  protected[ui] def drawComponent() {
    if (texture() != null) texture().texture.bind()

    glBegin(GL_TRIANGLES)

    renderSide(Vec3d(-1, -1, -1),  Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0, -1,  0), height())
    renderSide(Vec3d( 1, -1,  1), -Vec3d.UnitZ, Vec3d.UnitY, Vec3d( 1,  0,  0), width())
    renderSide(Vec3d( 1,  1, -1), -Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0,  1,  0), height())
    renderSide(Vec3d(-1,  1, -1), -Vec3d.UnitY, Vec3d.UnitZ, Vec3d(-1,  0,  0), width())
    renderSide(Vec3d( 1, -1, -1), -Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0, -1), depth())
    renderSide(Vec3d(-1, -1,  1),  Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0,  1), depth())

    glEnd()
  }

  def renderSide(start: Vec3d, uDelta: Vec3d, vDelta: Vec3d, normal: Vec3d, bulgeFactor: Double) {

    var Tau = 2 * Pi

    def cosineInterpolate(a: Double, b: Double, t: Double): Double = {
       val mu = (1.0 - cos(t * Pi)) / 2
       a * (1.0 - mu) + b * mu
    }


    val scaledStart = start * 0.5
    val size = subdivisions()
    val scale = Vec3d(width()/2, height()/2, depth()/2)

    def corner(u: Int, v: Int) {
      val nu = 1.0 * u / size
      val nv = 1.0 * v / size
      val pos = scaledStart + uDelta * nu + vDelta * nv
      pos *= scale

      // Calculate bulge
      val edgeDistance = 1.0 - 2 * max(abs(0.5 - nv), abs(0.5 - nu))
      //val bulge = cosineInterpolate(0, 1, edgeDistance)
      val bulge = sin(Tau / 4 * edgeDistance)
      pos += normal * (bulging() * bulge * bulgeFactor * 0.25)

      glTexCoord2d(nu, nv)
      glVertex3d(pos.x, pos.y, pos.z)
      if (color() != null) glColor4d(color().red, color().green, color().blue, color().alpha)
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