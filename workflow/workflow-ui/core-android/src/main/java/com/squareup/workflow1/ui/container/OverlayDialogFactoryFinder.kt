package com.squareup.workflow1.ui.container

import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewEnvironmentKey
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

/**
 * [ViewEnvironment] service object used by [Overlay.toDialogFactory] to find the right
 * [OverlayDialogFactoryScreenViewFactory]. The default implementation makes [AndroidOverlay]
 * work, and provides a default binding for [AlertOverlay].
 */
@WorkflowUiExperimentalApi
public interface OverlayDialogFactoryFinder {
  public fun <OverlayT : Overlay> getDialogFactoryForRendering(
    environment: ViewEnvironment,
    rendering: OverlayT
  ): OverlayDialogFactory<OverlayT> {
    val entry = environment[ViewRegistry].getEntryFor(rendering::class)

    @Suppress("UNCHECKED_CAST")
    return entry as? OverlayDialogFactory<OverlayT>
      ?: (rendering as? AndroidOverlay<*>)?.dialogFactory as? OverlayDialogFactory<OverlayT>
      ?: (rendering as? AlertOverlay)?.let {
        AlertOverlayDialogFactory as OverlayDialogFactory<OverlayT>
      }
      ?: throw IllegalArgumentException(
        "An OverlayDialogFactory should have been registered to display $this, " +
          "or that class should implement AndroidOverlay. Instead found $entry."
      )
  }

  public companion object : ViewEnvironmentKey<OverlayDialogFactoryFinder>(
    OverlayDialogFactoryFinder::class
  ) {
    override val default: OverlayDialogFactoryFinder = object : OverlayDialogFactoryFinder {}
  }
}
