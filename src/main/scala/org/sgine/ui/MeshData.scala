package org.sgine.ui

import _root_.org.sgine.core.Color
import java.util.ArrayList
import util.Random
import org.lwjgl.opengl.GL11._

import simplex3d.math.doublem._
import org.sgine.render.RenderImage
import org.lwjgl.BufferUtils
import java.nio.IntBuffer
import org.lwjgl.opengl.{ARBVertexBufferObject, GLContext}

/**
 * Contains mutable data for a model, including vertexes, normals, texture coordinates, and indexes.
 */
class MeshData {

  val vertexes = new ArrayList[Vec3d]()
  val normals = new ArrayList[Vec3d]()
  val textureCoordinates = new ArrayList[Vec2d]()
  val colors = new ArrayList[Color]()
  val indexes = new ArrayList[Int]()

  private var nextFreeIndex = 0

  def clear() {
    vertexes.clear
    normals.clear
    textureCoordinates.clear
    colors.clear
    indexes.clear
    nextFreeIndex = 0
  }

  def render(texture: RenderImage = null) {

/*
    if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object) renderWithVbo(texture)
    else renderWithDirectMode(texture)
     */

    renderWithDirectMode(texture)

  }
/*
  def renderWithVbo(texture: RenderImage) {
    def createVboId: Int = {
      val buffer: IntBuffer = BufferUtils.createIntBuffer(1)
      ARBVertexBufferObject.glGenBuffersARB(buffer);
      buffer.get(0);
    }

    val id: Int = createVboId

    // Use an interleaved Vertex Buffer Object

    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_COLOR_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);

    val stride = (3 + 3 + 4 + 2) * 4 // 3 for vertex, 3 for normal, 4 for colour and 2 for texture coordinates. * 4 for bytes

    // vertices
    var offset = 0 * 4; // 0 as its the first in the chunk, i.e. no offset. * 4 to convert to bytes.
    glVertexPointer(3, GL_FLOAT, stride, offset);

    // normals
    offset = 3 * 4; // 3 components is the initial offset from 0, then convert to bytes
    glNormalPointer(GL_FLOAT, stride, offset);

    // colours
    offset = (3 + 3) * 4; // (6*4) is the number of byte to skip to get to the colour chunk
    glColorPointer(4, GL_FLOAT, stride, offset);

    // texture coordinates
    offset = (3 + 3 + 2) * 4;
    glTexCoordPointer(2, GL_FLOAT, stride, offset);

    
  }

*/
  def renderWithDirectMode(texture: RenderImage) {
    if (texture != null) texture.texture.bind()

    glBegin(GL_TRIANGLES)

    var i = 0
    while (i < indexes.size) {
      val index = indexes.get(i)

      val tex = textureCoordinates.get(index)
      val color = colors.get(index)
      val pos = vertexes.get(index)
      val normal = normals.get(index)

      glTexCoord2d(tex.x, tex.y)
      glColor4d(color.red, color.green, color.blue, color.alpha)
      glNormal3d(normal.x, normal.y, normal.z)
      glVertex3d(pos.x, pos.y, pos.z)

      i += 1
    }

    glEnd()
    
  }

  def getVertex(index: Int): Vec3d = vertexes.get(index)
  def getNormal(index: Int): Vec3d = normals.get(index)
  def getTextureCoordinate(index: Int): Vec2d = textureCoordinates.get(index)
  def getColor(index: Int): Color = colors.get(index)
  def getIndex(i: Int): Int = indexes.get(i)

  def nrOfVertexes: Int = vertexes.size
  def nrOfIndexes: Int = indexes.size

  /* Adds a vertex and returns its ordinal number. */
  def addVertex(pos: Vec3d, normal: Vec3d = Vec3d.UnitY, textureCoordinate: Vec2d = Vec2d.Zero, color: Color = Color.White): Int = {
    vertexes.add(pos)
    normals.add(normal)
    textureCoordinates.add(textureCoordinate)
    colors.add(color)
    nextIndex()
  }

  def setVertex(index: Int, pos: Vec3d, normal: Vec3d = null, textureCoordinate: Vec2d = null, color: Color = null) {
    if (pos != null) vertexes.set(index, pos)
    if (normal != null) normals.set(index, normal)
    if (textureCoordinate != null) textureCoordinates.set(index, textureCoordinate)
    if (color != null) colors.set(index, color)
  }

  def setNormal(index: Int, normal: Vec3d) {
    normals.set(index, normal)
  }

  def setTextureCoordinate(index: Int, textureCoordinate: Vec2d) {
    textureCoordinates.set(index, textureCoordinate)
  }

  def setColor(index: Int, color: Color) {
    colors.set(index, color)
  }

  def addIndex(i: Int) {
    indexes.add(i)
  }

  def addTriangle(a: Int, b: Int, c: Int) {
    indexes.add(a)
    indexes.add(b)
    indexes.add(c)
  }

  def addQuad(a: Int, b: Int, c: Int, d: Int) {
    addTriangle(a, b, c)
    addTriangle(c, d, a)
  }

  def smoothAllNormals() {
    // TODO: Implement

    // Clear normals table (set to zero)

    // Create a number of sides array

    // Loop through all triangles

    // Calculate the normal for the triangle
    // Add normal to triangle corner vertex normals
    // Increase the number of sides counter for the corners


    // Loop through all normals, divide them with number of sides, if larger than 0 
  }


  def randomizeColors() {
    val rand = new Random()
    val max = colors.size
    var i = 0
    while (i < max) {
      colors.set(i, Color(rand.nextDouble, rand.nextDouble, rand.nextDouble))
      i += 1
    }
  }

  private def nextIndex(): Int = {
    val current = nextFreeIndex
    nextFreeIndex += 1
    current
  }
}