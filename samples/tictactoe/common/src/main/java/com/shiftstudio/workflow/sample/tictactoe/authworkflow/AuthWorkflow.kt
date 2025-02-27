package com.shiftstudio.workflow.sample.tictactoe.authworkflow

import android.os.Parcelable
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthResult.Authorized
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthResult.Canceled
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthService.AuthRequest
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthService.AuthResponse
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthService.SecondFactorRequest
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthState.Authorizing
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthState.AuthorizingSecondFactor
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthState.LoginPrompt
import com.shiftstudio.workflow.sample.tictactoe.authworkflow.AuthState.SecondFactorPrompt
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.rx2.asWorker
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.container.BackStackScreen
import kotlinx.parcelize.Parcelize

/**
 * We define this otherwise redundant typealias to keep composite workflows
 * that build on [AuthWorkflow] decoupled from it, for ease of testing.
 */
@OptIn(WorkflowUiExperimentalApi::class)
typealias AuthWorkflow = Workflow<Unit, AuthResult, BackStackScreen<Screen>>

sealed class AuthState : Parcelable {
    @Parcelize
    internal data class LoginPrompt(val errorMessage: String = "") : AuthState()

    @Parcelize
    internal data class Authorizing(
        val email: String,
        val password: String,
    ) : AuthState()

    @Parcelize
    internal data class SecondFactorPrompt(
        val tempToken: String,
        val errorMessage: String = "",
    ) : AuthState()

    @Parcelize
    internal data class AuthorizingSecondFactor(
        val tempToken: String,
        val secondFactor: String,
    ) : AuthState()
}

sealed class AuthResult {
    data class Authorized(val token: String) : AuthResult()
    object Canceled : AuthResult()
}

/**
 * Runs a set of login screens and pretends to produce an auth token,
 * via a pretend [authService].
 *
 * Demonstrates both client side validation (email format, must include "@")
 * and server side validation (password is "password").
 *
 * Includes a 2fa path for email addresses that include the string "2fa".
 * Token is "1234".
 */
@OptIn(WorkflowUiExperimentalApi::class)
class RealAuthWorkflow(private val authService: AuthService) : AuthWorkflow,
    StatefulWorkflow<Unit, AuthState, AuthResult, BackStackScreen<Screen>>() {

    override fun initialState(
        props: Unit,
        snapshot: Snapshot?,
    ): AuthState = LoginPrompt()

    override fun render(
        renderProps: Unit,
        renderState: AuthState,
        context: RenderContext,
    ): BackStackScreen<Screen> = when (renderState) {
        is LoginPrompt -> {
            BackStackScreen(
                LoginScreen(
                    renderState.errorMessage,
                    onLogin = context.eventHandler { email, password ->
                        state = when {
                            email.isValidEmail -> Authorizing(email, password)
                            else -> LoginPrompt(email.emailValidationErrorMessage)
                        }
                    },
                    onCancel = context.eventHandler { setOutput(Canceled) }
                )
            )
        }

        is Authorizing -> {
            context.runningWorker(
                authService.login(AuthRequest(renderState.email, renderState.password))
                    .asWorker()
            ) { handleAuthResponse(it) }

            BackStackScreen(
                LoginScreen(),
                AuthorizingScreen("Logging in…")
            )
        }

        is SecondFactorPrompt -> {
            BackStackScreen(
                LoginScreen(),
                SecondFactorScreen(
                    renderState.errorMessage,
                    onSubmit = context.eventHandler { secondFactor ->
                        (state as? SecondFactorPrompt)?.let { oldState ->
                            state = AuthorizingSecondFactor(oldState.tempToken, secondFactor)
                        }
                    },
                    onCancel = context.eventHandler { state = LoginPrompt() }
                )
            )
        }

        is AuthorizingSecondFactor -> {
            val request = SecondFactorRequest(renderState.tempToken, renderState.secondFactor)
            context.runningWorker(authService.secondFactor(request).asWorker()) {
                handleSecondFactorResponse(renderState.tempToken, it)
            }

            BackStackScreen(
                LoginScreen(),
                SecondFactorScreen(),
                AuthorizingScreen("Submitting one time token…")
            )
        }
    }

    private fun handleAuthResponse(response: AuthResponse) = action {
        when {
            response.isLoginFailure -> state = LoginPrompt(response.errorMessage)
            response.twoFactorRequired -> state = SecondFactorPrompt(response.token)
            else -> setOutput(Authorized(response.token))
        }
    }

    private fun handleSecondFactorResponse(tempToken: String, response: AuthResponse) = action {
        when {
            response.isSecondFactorFailure ->
                state = SecondFactorPrompt(tempToken, response.errorMessage)
            else -> setOutput(Authorized(response.token))
        }
    }

    /**
     * It'd be silly to restore an in progress login session, so saves nothing.
     */
    override fun snapshotState(state: AuthState): Snapshot? = null
}

internal val AuthResponse.isLoginFailure: Boolean
    get() = token.isEmpty() && errorMessage.isNotEmpty()

private val AuthResponse.isSecondFactorFailure: Boolean
    get() = token.isNotEmpty() && errorMessage.isNotEmpty()

internal val String.isValidEmail: Boolean
    get() = emailValidationErrorMessage.isBlank()

internal val String.emailValidationErrorMessage: String
    get() = if (indexOf('@') < 0) "Invalid address" else ""
