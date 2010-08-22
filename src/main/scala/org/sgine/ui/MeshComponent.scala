package org.sgine.ui

import org.sgine.property.AdvancedProperty
import org.sgine.render.RenderImage
import org.sgine.render.shape.ShapeMode
import scala.collection.JavaConversions._


/**
 * A component that creates the model as a mesh, and refreshes the mesh only when the properties change.
 */
abstract class MeshComponent extends ShapeComponent with InvalidatingComponent {

  private var meshData = new MeshData()

  /**
   * Texture image of the mesh, or null if no texture should be used.
   */
 // val texture = new AdvancedProperty[RenderImage](null, this)

  _mode := ShapeMode.Triangles

  /**
   * Create the mesh using current property values.
   */
  protected def buildMesh(meshData: MeshData)

  override protected[ui] def drawComponent() {
    doIfPropertiesChanged {
      meshData.clear()
      buildMesh(meshData)

      _vertices := meshData.vertexes
      _colors := meshData.colors
      _normal := meshData.normals
      _texcoords := meshData.textureCoordinates
    }

    super.drawComponent
    //meshData.render(texture())
  }
}