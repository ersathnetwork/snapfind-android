package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.AnalysisResultDto
import com.example.data.GeminiAiService
import com.example.data.ScreenshotRepository
import com.example.data.SnapFindDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    encodedUri: String,
    onBack: () -> Unit,
    onNavigateReview: (String, String, String) -> Unit // pass imagePath, contentHash, jsonAnalysis
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageUri = Uri.parse(Uri.decode(encodedUri))

    var isProcessing by remember { mutableStateOf(false) }
    var processingStage by remember { mutableStateOf("Ready to process") }
    var localImagePath by remember { mutableStateOf("") }
    var contentHash by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dao = remember { SnapFindDatabase.getDatabase(context).screenshotDao() }
    val repository = remember { ScreenshotRepository(dao, context) }

    LaunchedEffect(imageUri) {
        scope.launch {
            processingStage = "Reading image..."
            val result = repository.saveImportedImage(imageUri)
            if (result != null) {
                localImagePath = result.first
                contentHash = result.second
            } else {
                errorMessage = "Failed to load image file."
            }
        }
    }

    fun startAnalysis(mode: String) {
        if (localImagePath.isBlank()) return
        isProcessing = true
        scope.launch {
            processingStage = "Extracting text..."
            kotlinx.coroutines.delay(600)
            processingStage = "Organizing details with AI..."

            val analysis = GeminiAiService.analyzeScreenshot(context, localImagePath)

            processingStage = "Ready to review"
            isProcessing = false

            // Serialize analysis to JSON or pass via repository/shared state
            val id = UUID.randomUUID().toString()
            // Save draft
            repository.saveScreenshotRecord(
                id = id,
                localImagePath = localImagePath,
                contentHash = contentHash,
                analysis = analysis
            )
            onNavigateReview(id, localImagePath, contentHash)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Screenshot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (localImagePath.isNotBlank()) {
                        AsyncImage(
                            model = java.io.File(localImagePath),
                            contentDescription = "Imported screenshot preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = processingStage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "Choose how to organize this screenshot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SnapFind will extract visible text, titles, categories, and dates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (!isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { startAnalysis("AI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Organize with AI", style = MaterialTheme.typography.titleMedium)
                    }

                    OutlinedButton(
                        onClick = { startAnalysis("TEXT_ONLY") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Extract text only", style = MaterialTheme.typography.titleMedium)
                    }

                    TextButton(
                        onClick = { startAnalysis("MANUAL") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Organize manually")
                    }
                }
            }
        }
    }
}
