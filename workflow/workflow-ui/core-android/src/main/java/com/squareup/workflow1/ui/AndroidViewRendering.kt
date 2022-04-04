package com.squareup.workflow1.ui

@Suppress("DEPRECATION")
@Deprecated("Use AndroidScreen")
@WorkflowUiExperimentalApi
public interface AndroidViewRendering<V : AndroidViewRendering<V>> {
  /**
   * Used to build instances of [android.view.View] as needed to
   * display renderings of this type.
   */
  public val viewFactory: ViewFactory<V>
}
