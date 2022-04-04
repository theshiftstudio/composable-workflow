package com.shiftstudio.workflow.sample.tictactoe.authworkflow

import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
val AuthViewFactories = ViewRegistry(
    AuthorizingViewFactory,
    LoginViewFactory,
    SecondFactorViewFactory
)
