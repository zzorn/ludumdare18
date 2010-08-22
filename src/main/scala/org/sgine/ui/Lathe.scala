package org.sgine.ui

import scala.math.Pi
import scala.math.cos
import scala.math.sin
import scala.math.floor
import scala.math.ceil
import org.sgine.core.Color

import simplex3d.math.doublem._

trait Projection {
  /*
   * Calculates the texture coordinates (in range 0..1) for the specified target coordinates (in range 0..1)
   */
  def textureCoordinates(x: Double, y: Double): Vec2d
}

case object LinearProjection extends Projection {
  def textureCoordinates(x: Double, y: Double) = Vec2d(x, y)
}

case object PolarProjection extends Projection {
  def textureCoordinates(x: Double, y: Double) = Vec2d(cos(x * 2*Pi) * y, sin(x * 2*Pi) * y)
}

case class LinearFunction(scale: Double = 1, offset: Double = 0) extends Func.Signal {
  override def apply(v : Double) : Double = v * scale + offset
}

case class LinearInterpolator(a: Double, b: Double) extends Func.Signal {
  override def apply(t : Double) : Double = (1.0 - t) * a + t * b
}

case class ConstantFunction(value: Double = 0) extends Function1[Double, Double] {
  override def apply(v : Double) : Double = value
}

case class PathFunction(xFunc: Func.Signal, yFunc: Func.Signal, zFunc: Func.Signal) extends Func.Path {
  def apply(t: Double) : Vec3d = Vec3d(xFunc(t), yFunc(t), zFunc(t))
}

object Func {
  type Signal = Function1[Double, Double]
  type Path = Function1[Double, Vec3d]
  type Envelope = Function2[Double, Double, Vec3d]
}

case class ZeroToOneGradient(values: Double*) extends (Double => Double) {
  val num = values.size
  val slizeSize = if (num > 1) 1.0 / (num - 1) else 1
  
  override def apply(v: Double): Double = {
    if (num == 0) 0
    else if (num == 1) values(0)
    else if (v < 0) values.head
    else if (v > 1) values.last
    else {
      val slizePos = v / slizeSize
      val prev = floor(slizePos).intValue
      val next = ceil(slizePos).intValue
      if (next >= values.size) values(prev)
      else if (prev < 0) values(next)
      else {
        val t = slizePos - prev
        values(prev) * (1.0 - t) + values(next) * t
      }
    }
  }
}

class OutlineEnvelope(verticalOutline: Double => Double,
                      radialOutline:   Double => Double)
        extends RadialEnvelope( (u: Double, v: Double) => {radialOutline(u) * verticalOutline(v)} )

class RadialEnvelope(heightMap: (Double, Double) => Double) extends Func.Envelope {
  override def apply(u: Double, v: Double): Vec3d = {
    val Tau = Pi*2
    val angle = Tau * u
    val radius = heightMap(u, v)
    val x = radius * cos(angle)
    val y = v
    val z = radius * sin(angle)
    Vec3d(x, y, z)
  }
}

object IdentityFunction extends LinearFunction(1, 0)
object OneFunction extends ConstantFunction(1)
object StraightPath extends PathFunction(ConstantFunction(), LinearFunction(), ConstantFunction())

case class CylinderFunction(radiusOverLength: Func.Signal = OneFunction,
                            radiusShape: Func.Signal = OneFunction,
                            path: Func.Path=StraightPath) extends Func.Envelope {
  override def apply(u : Double, v : Double) : Vec3d = {
    val uAsRadians: Double = u * 2 * Pi
    val radiusScale = radiusShape(u) * radiusOverLength(v)
    val x = path(v).x + cos(uAsRadians) * radiusScale
    val y = path(v).y
    val z = path(v).z + sin(uAsRadians) * radiusScale

    Vec3d(x, y, z)
  }
}

// TODO: Add a few more useful surface envelope functions, and move them to a separate package.

/*
 * Parametric cylinder shaped primitive with a specified number of sides, segments, and a surface function.
 */
class Lathe extends MeshComponent {

  val segments = intProp(10)
  val sides = intProp(10)
  val lengthScale = doubleProp(1)
  val radialScale = doubleProp(1)
  val colorFunction = prop[(Double,Double)=>Color]( (u:Double, v:Double) => Color.White)
  val textureProjection = prop[Projection](LinearProjection)
  val surface = prop[Func.Envelope]( CylinderFunction() )

  protected def buildMesh(meshData: MeshData) {
    val segs = segments()
    val sids = sides()
    val surf = surface()
    val texProj = textureProjection()
    val colorFun = colorFunction()
    val radScale = radialScale()
    val lenScale = lengthScale()
    val scale = Vec3d(radScale, lenScale, radScale)

    var segment = 0
    while (segment < segs) {

      var side = 0
      while (side < sids) {

        val u = 1.0*side / (sids - 1.0)
        val v = 1.0*segment / (segs - 1.0)

        val pos = surf(u, v) * scale
        val tex = texProj.textureCoordinates(u, v)
        val col = colorFun(u, v)
        val nor = Vec3d.UnitY // Calculated afterwards

        val index = meshData.addVertex(pos, nor, tex, col)

        if (segment > 0 && side > 0) meshData.addQuad(index, index-1, index -1 -sids, index-sids)
        if (segment > 0 && side == 0) meshData.addQuad(index, index + sids -1, index -1, index-sids) // Stitch together into a cylinder shape

        side += 1
      }

      segment += 1
    }

    meshData.smoothAllNormals()
  }


}
