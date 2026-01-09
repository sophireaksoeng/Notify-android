package com.team.notify

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.notify.ui.auth.AuthScreen
import com.team.notify.ui.auth.AuthViewModel
import com.team.notify.ui.spaces.SpacesScreen
import com.team.notify.taskflow.presentation.onboarding.OnboardingScreen
import com.team.notify.taskflow.presentation.onboarding.OnboardingViewModel
import com.team.notify.taskflow.presentation.profile.ProfileScreen
import com.team.notify.taskflow.presentation.workspace.WorkspaceScreen

@Composable
fun AppRoot(
    authViewModel: AuthViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authState.userId != null
    val hasCompletedOnboarding by onboardingViewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

    if (hasCompletedOnboarding == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var currentScreen by remember {
        mutableStateOf(
            when {
                hasCompletedOnboarding == false -> "onboarding"
                !isLoggedIn -> "auth"
                else -> "spaces"
            }
        )
    }

    var currentSpaceId by remember { mutableStateOf("notify-db") }
    var previousScreen by remember { mutableStateOf("spaces") }

    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding == true) {
            currentScreen = if (!isLoggedIn) "auth" else "spaces"
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentScreen != "auth" && currentScreen != "onboarding") {
            previousScreen = currentScreen
            currentScreen = "auth"
        } else if (isLoggedIn && currentScreen == "auth") {
            currentScreen = previousScreen
        }
    }

    BackHandler(enabled = currentScreen == "workspace" || currentScreen == "profile") {
        currentScreen = "spaces"
    }

    when (currentScreen) {
        "onboarding" -> {
            OnboardingScreen(
                onOnboardingComplete = {
                    onboardingViewModel.completeOnboarding()
                }
            )
        }
        "spaces" -> {
            SpacesScreen(
                onNavigateToWorkspace = { spaceId ->
                    currentSpaceId = spaceId
                    currentScreen = "workspace"
                },
                onNavigateToProfile = {
                    if (isLoggedIn) {
                        currentScreen = "profile"
                    } else {
                        currentScreen = "auth"
                    }
                },
                onCreateSpace = {

                }
            )
        }
        "auth" -> {
            AuthScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    currentScreen = if (previousScreen == "profile") "profile" else "spaces"
                }
            )
        }
        "profile" -> {
            ProfileScreen(
                onBack = {
                    currentScreen = "spaces"
                },
                onLogout = {
                    authViewModel.logout()
                    currentScreen = "auth"
                }
            )
        }
        "workspace" -> {
            WorkspaceScreen(
                spaceId = currentSpaceId,
                onBack = {
                    currentScreen = "spaces"
                }
            )
        }
    }
}