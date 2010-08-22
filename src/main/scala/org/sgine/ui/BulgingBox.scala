package org.sgine.ui

import org.sgine.core.Color
import org.sgine.render.RenderImage
import scala.math._
import org.lwjgl.opengl.GL11._
import org.sgine.property.{ModifiableProperty, AdvancedProperty}
import simplex3d.math.doublem.{Vec2d, Vec3d}
//import simplex3d.math.doublem.DoubleMath._

// NOTE: Has to be in the org.sgine.ui package, because the render method is declared as protected[ui] TODO: Change this in sgine

// TODO: Add shading?
class BulgingBox() extends MeshComponent {

  def this(width: Double,
           height: Double,
           depth: Double,
           bulging: Double = 0.0,
           subdivisions: Int = 5,
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
    //this.texture := texture
  }


  /** Width of the box (size along x axis). */
  val width = doubleProp(100.0)

  /** Height of the box (size along y axis). */
  val height = doubleProp(100)

  /** Depth of the box (size along z axis). */
  val depth = doubleProp(100)

  /** Amount the box bulges out (positive) or in (negative).  0 = no bulging. */
  val bulging = doubleProp(0.2)

  val subdivisions = intProp(5)

  val centerColor = colorProp(Color.White)

  protected def buildMesh(meshData: MeshData) {
    val d = depth()
    val h = height()
    val w = width()

    def mix(a: Double, b: Double) = (a + b) / 2

    buildSide(meshData, Vec3d(-1, -1, -1),  Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0, -1,  0), mix(w,d))
    buildSide(meshData, Vec3d( 1, -1,  1), -Vec3d.UnitZ, Vec3d.UnitY, Vec3d( 1,  0,  0), mix(h,d))
    buildSide(meshData, Vec3d( 1,  1, -1), -Vec3d.UnitX, Vec3d.UnitZ, Vec3d( 0,  1,  0), mix(w,d))
    buildSide(meshData, Vec3d(-1,  1, -1), -Vec3d.UnitY, Vec3d.UnitZ, Vec3d(-1,  0,  0), mix(h,d))
    buildSide(meshData, Vec3d( 1, -1, -1), -Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0, -1), mix(w,h))
    buildSide(meshData, Vec3d(-1, -1,  1),  Vec3d.UnitX, Vec3d.UnitY, Vec3d( 0,  0,  1), mix(w,h))
  }

  def buildSide(meshData: MeshData, start: Vec3d, uDelta: Vec3d, vDelta: Vec3d, normal: Vec3d, bulgeFactor: Double) {

    var Tau = 2 * Pi

    def cosineInterpolate(a: Double, b: Double, t: Double): Double = {
       val mu = (1.0 - cos(t * Pi)) / 2
       a * (1.0 - mu) + b * mu
    }

    def lerp(a: Double, b: Double, t: Double): Double = a * (1.0 - t) + b * t


    val scaledStart = start * 0.5
    val size = subdivisions()
    val scale = Vec3d(width()/2, height()/2, depth()/2)

    def corner(u: Int, v: Int): Int = {
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

      val edgeColor: Color = if(color() != null) color() else Color.White
      val cColor: Color = if(centerColor() != null) centerColor() else Color.White
      def clerp(c1: Double, c2: Double): Double = c1 * (1 - distance) + c2 * distance
      val col = Color(clerp(edgeColor.red, cColor.red),
                      clerp(edgeColor.green, cColor.green),
                      clerp(edgeColor.blue, cColor.blue),
                      clerp(edgeColor.alpha, cColor.alpha))

      meshData.addVertex(pos, Vec3d.UnitY, Vec2d(nu, nv), col)
    }

    var v = 0
    while (v <= size) {
      var u = 0
      while (u <= size) {

        // Create vertex
        val index = corner(u, v)

        // Add triangles
        if (u < size && v < size) {
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
          def ind(du: Int, dv: Int): Int = index + du + dv * (size+1)

          if ((u <= size / 2 && v <= size / 2) ||
              (u > size / 2 && v > size / 2)) {

            meshData.addIndex(ind(0,0))
            meshData.addIndex(ind(0,1))
            meshData.addIndex(ind(1,1))

            meshData.addIndex(ind(1,1))
            meshData.addIndex(ind(1,0))
            meshData.addIndex(ind(0,0))
          }
          else {
            meshData.addIndex(ind(0,0))
            meshData.addIndex(ind(0,1))
            meshData.addIndex(ind(1,0))

            meshData.addIndex(ind(0,1))
            meshData.addIndex(ind(1,1))
            meshData.addIndex(ind(1,0))
          }
        }

        u += 1
      }
      v += 1
    }
  }

}