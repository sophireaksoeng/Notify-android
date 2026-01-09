package com.team.notify.taskflow.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.team.notify.taskflow.data.entities.UserProfileEntity
import com.team.notify.taskflow.data.entities.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You could show a snackbar here
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Light background
    ) {
        // Top spacing for system UI
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header - Light theme style
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF4A90E2),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Title
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1F2937)
                )
                
                // More options
                IconButton(
                    onClick = { /* TODO: Show options */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color(0xFF4A90E2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Main content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                userProfile == null -> {
                    // User is logged out, navigate back to spaces
                    LaunchedEffect(Unit) {
                        onBack()
                    }
                }
                else -> {
                    // Show existing profile
                    userProfile?.let { profile ->
                        ProfileContent(
                            profile = profile,
                            onEditProfile = { showEditDialog = true },
                            onShowPreferences = { showPreferencesDialog = true },
                            onLogout = onLogout,
                            onSyncProfile = { viewModel.syncProfileFromCloud() }
                        )
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog && userProfile != null) {
        userProfile?.let { profile ->
            EditProfileDialog(
                profile = profile,
                onDismiss = { showEditDialog = false },
                onSave = { displayName, username, bio, avatarUrl ->
                    viewModel.updateProfile(
                        displayName = displayName,
                        username = username,
                        bio = bio,
                        avatarUrl = avatarUrl
                    )
                    showEditDialog = false
                }
            )
        }
    }

    // Preferences Dialog
    if (showPreferencesDialog && userProfile?.preferences != null) {
        userProfile?.let { profile ->
            profile.preferences?.let { preferences ->
                PreferencesDialog(
                    preferences = preferences,
                    onDismiss = { showPreferencesDialog = false },
                    onSave = { newPreferences ->
                        viewModel.updateProfile(preferences = newPreferences)
                        showPreferencesDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfileEntity,
    onEditProfile: () -> Unit,
    onShowPreferences: () -> Unit,
    onLogout: () -> Unit,
    onSyncProfile: () -> Unit
) {
    // Profile header card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Profile info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar and info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color(0xFF4A90E2).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.avatarUrl != null) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = profile.displayName.firstOrNull()?.uppercase() ?: "U",
                                color = Color(0xFF4A90E2),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "@${profile.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Email verification
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (profile.isEmailVerified) Icons.Default.Verified else Icons.Default.Email,
                                contentDescription = null,
                                tint = if (profile.isEmailVerified) Color(0xFF10B981) else Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (profile.isEmailVerified) "Verified" else "Not Verified",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (profile.isEmailVerified) Color(0xFF10B981) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bio
            if (!profile.bio.isNullOrBlank()) {
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Business,
                    label = "Workspaces",
                    value = "0",
                    color = Color(0xFF4A90E2)
                )
                
                StatItem(
                    icon = Icons.Default.Task,
                    label = "Tasks",
                    value = "0",
                    color = Color(0xFF10B981)
                )
                
                StatItem(
                    icon = Icons.Default.Description,
                    label = "Pages",
                    value = "0",
                    color = Color(0xFFF59E0B)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Edit Profile button
            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // Settings section
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Settings items
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                ProfileSettingItem(
                    icon = Icons.Default.Settings,
                    title = "Preferences",
                    subtitle = "Customize your app experience",
                    onClick = onShowPreferences
                )
                
                ProfileSettingItem(
                    icon = Icons.Default.CloudSync,
                    title = "Sync Profile",
                    subtitle = "Sync profile data with cloud",
                    onClick = onSyncProfile
                )
                
                ProfileSettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification settings",
                    onClick = { /* TODO: Implement notifications */ }
                )
                
                ProfileSettingItem(
                    icon = Icons.Default.Security,
                    title = "Security",
                    subtitle = "Password and authentication settings",
                    onClick = { /* TODO: Implement security */ }
                )
                
                ProfileSettingItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help and contact support",
                    onClick = { /* TODO: Implement help */ }
                )
                
                // Logout item
                ProfileSettingItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = onLogout,
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF1F2937)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun ProfileSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFEF4444) else Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Color(0xFFEF4444) else Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(20.dp)
        )
    }
    
    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 0.dp),
        color = Color(0xFFE5E7EB),
        thickness = 0.5.dp
    )
}

@Composable
private fun ProfileActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CreateProfileForm(
    onCreateProfile: (String, String, String?) -> Unit,
    isLoading: Boolean
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onCreateProfile(displayName, username, bio.ifBlank { null })
            },
            enabled = displayName.isNotBlank() && username.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Profile")
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return format.format(date)
}
