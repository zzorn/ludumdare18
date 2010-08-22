package org.sgine.ui

import org.sgine.event.{ProcessingMode, Event, EventHandler}
import org.sgine.core.Color
import org.sgine.property.AdvancedProperty

/**
 * A component that can invalidate some state when any properties declared with its utility methods change.
 */
trait InvalidatingComponent extends Component {

  private var valid = false

  /**
   * Forces a re-creation of any data that is dependent on the properies of this component.
   */
  final def invalidate() = valid = false

  final def isValid() = valid

  final def validate() = valid = true

  /**
   * Executes the code block if this component has been invalidated since the last call because properties have changed.
   */
  final def doIfPropertiesChanged( block: => Unit) {
    if (!valid) {
      block
      valid = true
    }
  }

  /**
   * Convenience event handler that can be added as a listener to a property that should cause dependent data to be invalidated and updated when changed.
   */
  protected val invalidationHandler: EventHandler = EventHandler((e:Event) => invalidate, ProcessingMode.Blocking)

  // Shortcuts to ease definition of properties in decendant classes
  protected def doubleProp(initialValue: Double): AdvancedProperty[Double] = new AdvancedProperty[Double](initialValue, this, null, null, invalidationHandler)
  protected def intProp(initialValue: Int): AdvancedProperty[Int]          = new AdvancedProperty[Int]   (initialValue, this, null, null, invalidationHandler)
  protected def colorProp(initialValue: Color): AdvancedProperty[Color]    = new AdvancedProperty[Color] (initialValue, this, null, null, invalidationHandler)
  protected def prop[T](initialValue: T)(implicit manifest: Manifest[T]): AdvancedProperty[T] = new AdvancedProperty[T] (initialValue, this, null, null, invalidationHandler)

}