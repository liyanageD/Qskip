package com.example.qskip.admin.flyers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.qskip.domain.model.Flyer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFlyersScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminFlyersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var titleInput by remember { mutableStateOf("") }
    var subtitleInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            imageBytes = bytes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banner Flyers Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowAddDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Flyer")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading && uiState.flyers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.flyers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No home banner flyers created yet.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.flyers, key = { it.flyerId }) { flyer ->
                        FlyerCard(
                            flyer = flyer,
                            onToggleActive = { active -> viewModel.toggleFlyer(flyer.flyerId, active) },
                            onDelete = { viewModel.deleteFlyer(flyer.flyerId) }
                        )
                    }
                }
            }

            if (uiState.showAddDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowAddDialog(false) },
                    title = { Text("Create New Banner Flyer") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Title (e.g. Summer 2026 Collection)") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = subtitleInput,
                                onValueChange = { subtitleInput = it },
                                label = { Text("Subtitle (e.g. Up to 40% off selected styles)") },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Flyer Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (selectedImageUri == null) "Select Flyer Image" else "Change Image")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.saveFlyerWithImage(
                                    title = titleInput,
                                    subtitle = subtitleInput,
                                    imageBytes = imageBytes,
                                    existingImageUrl = "",
                                    active = true
                                )
                                titleInput = ""
                                subtitleInput = ""
                                selectedImageUri = null
                                imageBytes = null
                            },
                            enabled = titleInput.isNotBlank() && (imageBytes != null)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowAddDialog(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FlyerCard(
    flyer: Flyer,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (flyer.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = flyer.imageUrl,
                    contentDescription = flyer.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(flyer.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (flyer.subtitle.isNotBlank()) {
                        Text(flyer.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = flyer.active,
                        onCheckedChange = onToggleActive
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
