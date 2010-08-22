package org.sgine.ui

import org.sgine.property.AdvancedProperty
import org.sgine.render.RenderImage

/**
 * A component that creates the model as a mesh, and refreshes the mesh only when the properties change.
 */
abstract class MeshComponent extends Component with InvalidatingComponent {

  private var meshData = new MeshData()

  /**
   * Texture image of the mesh, or null if no texture should be used.
   */
  val texture = new AdvancedProperty[RenderImage](null, this)

  /**
   * Create the mesh using current property values.
   */
  protected def buildMesh(meshData: MeshData)

  protected[ui] def drawComponent() {
    doIfPropertiesChanged {
      meshData.clear()
      buildMesh(meshData)
    }

    meshData.render(texture())
  }
}