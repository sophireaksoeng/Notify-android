package com.team.notify

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.notify.ui.auth.AuthScreen
import com.team.notify.ui.spaces.SpacesScreen
import com.team.notify.taskflow.presentation.onboarding.OnboardingScreen
import com.team.notify.taskflow.presentation.onboarding.OnboardingViewModel
import com.team.notify.taskflow.presentation.profile.ProfileScreen
import com.team.notify.taskflow.presentation.workspace.WorkspaceScreen

@Composable
fun AppRoot(
    authViewModel: com.team.notify.ui.auth.AuthViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authState.userId != null
    val hasCompletedOnboarding by onboardingViewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    
    var currentScreen by remember { 
        mutableStateOf(
            when {
                !hasCompletedOnboarding -> "onboarding"
                !isLoggedIn -> "auth"
                else -> "spaces" // Start with spaces (local-only) after onboarding
            }
        ) 
    }
    var currentSpaceId by remember { mutableStateOf("notify-db") }
    
    // Handle onboarding completion
    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding) {
            currentScreen = "spaces" // Go to spaces after onboarding
        }
    }
    
    var previousScreen by remember { mutableStateOf("spaces") }
    
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentScreen != "auth" && currentScreen != "onboarding") {
            previousScreen = currentScreen
            currentScreen = "auth"
        } else if (isLoggedIn && currentScreen == "auth") {
            currentScreen = previousScreen
        }
    }
    
    // Handle system back button
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
                    // This will trigger the dialog in SpacesScreen
                }
            )
        }
        "auth" -> {
            AuthScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Navigate back to the screen the user came from
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
                    currentScreen = "spaces"
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
