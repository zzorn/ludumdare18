package org.sgine.ui

import java.util.ArrayList
import ludumdare18.models.Block
import scala.collection.JavaConversions._
import simplex3d.math.doublem.Vec3d
import ludumdare18.Boxy

/**
 * Terrain made from blocks.
 */
class Terrain extends CompositeComponent with InvalidatingComponent {

  private val grid = new ArrayList[Component]()

  val terrainSize = intProp(10)

  def createBlock(): Component = {
    new Boxy()
    //new Block()
  }
  
  def initTerrain() {
    grid foreach {this -= _ }
    grid.clear

    val size = terrainSize()
    println("Initializing terrain of size " + size)
    val gridSize = 200.0
    var y = 0
    while (y < size) {
      var x = 0
      while (x < size) {
        val block = createBlock()
        block.location.x := (x-size/2) * gridSize
        block.location.y := 0
        block.location.z := -1000 + y /*(y-size/2)*/ * gridSize

        this += block
        grid += block
        
        x += 1
      }
      y += 1
    }
  }

  override protected[ui] def drawComponent() = {
    doIfPropertiesChanged {
      initTerrain
    }

    super.drawComponent()
  }
}