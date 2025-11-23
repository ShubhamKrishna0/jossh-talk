package com.example.josh.ui



import androidx.navigation.NavBackStackEntry
import androidx.compose.runtime.Composable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap  // ✅ IMPORTANT FIX
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.josh.data.TaskRepository
import com.example.josh.data.ProductsResponse
import com.example.josh.recorder.AudioRecorder
import com.example.josh.network.ProductsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


// --- palette omitted for brevity (same as before) ---
private val PrimaryTeal = Color(0xFF00897B)
private val TealLight = Color(0xFF4DB6AC)
private val TealSoft = Color(0xFFE0F2F1)
private val SoftBackground = Color(0xFFF5F7F8)

object Routes {
    const val Start = "start"
    const val Noise = "noise"
    const val Select = "select"
    const val TextRead = "text_read"
    const val ImageDesc = "image_desc"
    const val Photo = "photo"
    const val History = "history"
    const val TaskDetails = "task_details"
}

@Composable
fun AppNavHost(
    repo: TaskRepository,
    recorder: AudioRecorder,
    productsRepository: ProductsRepository
) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.Start
    ) {

        composable(Routes.Start) { StartScreen(nav) }
        composable(Routes.Noise) { NoiseTestScreen(nav) }
        composable(Routes.Select) { TaskSelectionScreen(nav) }
        composable(Routes.TextRead) { 
            TextReadingScreen(nav, repo, recorder, productsRepository) 
        }
        composable(Routes.ImageDesc) { 
            ImageDescriptionScreen(nav, repo, recorder, productsRepository) 
        }
        composable(Routes.Photo) { PhotoCaptureScreen(nav, repo, recorder) }
        composable(Routes.History) { TaskHistoryScreen(nav, repo) }

        // ✅ PLACE IT HERE, INSIDE NAVHOST
        composable("${Routes.TaskDetails}/{taskId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("taskId")
            TaskDetailsScreen(nav, repo, id)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommonScaffold(
    nav: NavHostController?,
    title: String,
    showBack: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = SoftBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    if (showBack && nav != null) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryTeal
                )
            )
        },
        content = content
    )
}


@Composable
fun StartScreen(nav: NavHostController) {
    CommonScaffold(nav = null, title = "Welcome") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Josh Audio Tasks",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Let’s start with a sample task for practice.\n\nPehele hum ek sample task karte hain.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(32.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "We'll check your environment and guide you through tasks like:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("• Text Reading\n• Image Description\n• Photo Capture",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { nav.navigate(Routes.Noise) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = Color.White
                )
            ) {
                Text("Start Sample Task", fontSize = 17.sp)
            }
        }
    }
}

@Composable
fun NoiseTestScreen(nav: NavHostController) {
    var reading by remember { mutableStateOf(20f) }
    var avg by remember { mutableStateOf(20f) }
    val readings = remember { mutableStateListOf<Float>() }
    val scope = rememberCoroutineScope()

    CommonScaffold(nav = nav, title = "Noise Check", showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Make sure you're in a quiet place before recording.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(24.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Noise Level",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryTeal
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(TealSoft, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${reading.toInt()} dB",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Average: ${avg.toInt()} dB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    readings.clear()
                    scope.launch {
                        repeat(30) {
                            val v = (20..55).random().toFloat()
                            reading = v
                            readings.add(v)
                            avg = readings.average().toFloat()
                            delay(100)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Start Noise Test")
            }

            Spacer(Modifier.height(16.dp))

            if (avg < 40f) {
                Text("✅ Good to proceed", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { nav.navigate(Routes.Select) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue")
                }
            } else if (readings.isNotEmpty()) {
                Text(
                    "❗ Please move to a quieter place",
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TaskSelectionScreen(nav: NavHostController) {
    CommonScaffold(nav = nav, title = "Select Task", showBack = false) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Choose the type of task you want to perform.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { nav.navigate(Routes.TextRead) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("📖 Text Reading Task", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Read a short passage aloud in your language.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { nav.navigate(Routes.ImageDesc) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("🖼 Image Description Task", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Describe what you see in a product image.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { nav.navigate(Routes.Photo) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("📸 Photo Capture Task", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Capture a photo and describe it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { nav.navigate(Routes.History) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📁 View Task History")
            }
        }
    }
}

@Composable
fun TextReadingScreen(
    nav: NavHostController,
    repo: TaskRepository,
    recorder: AudioRecorder,
    productsRepository: ProductsRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sampleText by remember { mutableStateOf<String?>(null) }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var startTime by remember { mutableStateOf(0L) }
    var durationSec by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var checked1 by remember { mutableStateOf(false) }
    var checked2 by remember { mutableStateOf(false) }
    var checked3 by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                // use the retrofit-backed repository
                val product = productsRepository.fetchRandomProduct()
                sampleText = product?.description ?: "No description found"
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Failed to fetch text: ${e.localizedMessage ?: "unknown"}"
                sampleText = "Default text: Unable to fetch."
            } finally {
                isLoading = false
            }
        }
    }


    CommonScaffold(nav = nav, title = "Text Reading", showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                "Read the passage aloud in your native language.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(12.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Passage",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryTeal
                    )
                    Spacer(Modifier.height(8.dp))
                    when {
                        isLoading -> Text("Loading text...", color = Color.Gray)
                        sampleText != null -> Text(
                            sampleText!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Press and hold the mic button while you read.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(110.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                errorMsg = null
                                startTime = SystemClock.elapsedRealtime()
                                val path = recorder.startRecording()
                                recordingPath = path
                                val released = try {
                                    awaitRelease()
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                                val out = recorder.stopRecording()
                                val elapsed =
                                    ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
                                durationSec = elapsed
                                if (elapsed < 10) {
                                    errorMsg = "Recording too short (min 10 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                } else if (elapsed > 20) {
                                    errorMsg = "Recording too long (max 20 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                }
                            })
                        },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Text("🎙", fontSize = 28.sp)
                        Text(
                            "Hold to Record",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFC62828))
            }

            recordingPath?.let { path ->
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!playing) {
                                recorder.play(path) { playing = false }
                                playing = true
                            } else {
                                recorder.stopPlayback()
                                playing = false
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                    ) {
                        Text(if (playing) "Stop" else "Play")
                    }
                    Text("Duration: ${durationSec}s")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Before submitting, confirm:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked1, onCheckedChange = { checked1 = it })
                Spacer(Modifier.width(4.dp))
                Text("No background noise")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked2, onCheckedChange = { checked2 = it })
                Spacer(Modifier.width(4.dp))
                Text("No mistakes while reading")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked3, onCheckedChange = { checked3 = it })
                Spacer(Modifier.width(4.dp))
                Text("Beech me koi galti nahi hai")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        recordingPath = null
                        durationSec = 0
                        errorMsg = null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Record again")
                }
                Button(
                    onClick = {
                        scope.launch {
                            if (recordingPath != null && checked1 && checked2 && checked3 && sampleText != null) {
                                val task = com.example.josh.data.Task(
                                    id = UUID.randomUUID().toString(),
                                    task_type = "text_reading",
                                    text = sampleText,
                                    audio_path = recordingPath,
                                    duration_sec = durationSec,
                                    timestamp = repo.nowIso()
                                )
                                repo.saveTask(task)
                                nav.navigate(Routes.Select) {
                                    popUpTo(Routes.Select)
                                }
                            }
                        }
                    },
                    enabled = (recordingPath != null && checked1 && checked2 && checked3 && sampleText != null),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

@Composable
fun ImageDescriptionScreen(
    nav: NavHostController,
    repo: TaskRepository,
    recorder: AudioRecorder,
    productsRepository: ProductsRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var productTitle by remember { mutableStateOf<String?>(null) }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var durationSec by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                val product = productsRepository.fetchRandomProduct()
                if (product != null) {
                    imageUrl = product.images.firstOrNull() ?: product.thumbnail
                    productTitle = product.title
                } else {
                    errorMsg = "No product found"
                }

                // ---- FIXED: PROPER IMAGE LOADING ----
                imageUrl?.let { imgUrlStr ->
                    try {
                        bmp = withContext(Dispatchers.IO) {
                            val connection = URL(imgUrlStr).openConnection()
                            connection.connectTimeout = 8000
                            connection.readTimeout = 8000
                            val stream = connection.getInputStream()
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMsg = "Failed to load image: ${e.localizedMessage ?: "unknown"}"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Failed to fetch product: ${e.localizedMessage ?: "unknown"}"
            } finally {
                isLoading = false
            }
        }
    }


    CommonScaffold(nav = nav, title = "Image Description", showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                "Look at the product image and describe what you see in your native language.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Text("Loading product...", color = Color.Gray)
            } else if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFC62828))
            }

            Spacer(Modifier.height(8.dp))

            productTitle?.let {
                Text(
                    "Product: $it",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryTeal
                )
                Spacer(Modifier.height(8.dp))
            }

            bmp?.let {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(220.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Press and hold the mic button to record your description.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(110.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                errorMsg = null
                                val start = SystemClock.elapsedRealtime()
                                val path = recorder.startRecording()
                                val released = try {
                                    awaitRelease()
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                                val out = recorder.stopRecording()
                                val elapsed =
                                    ((SystemClock.elapsedRealtime() - start) / 1000).toInt()
                                durationSec = elapsed
                                recordingPath = out
                                if (elapsed < 10) {
                                    errorMsg = "Recording too short (min 10 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                } else if (elapsed > 20) {
                                    errorMsg = "Recording too long (max 20 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                }
                            })
                        },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Text("🎙", fontSize = 28.sp)
                        Text(
                            "Hold to Record",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFC62828))
            }

            recordingPath?.let { path ->
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!playing) {
                                recorder.play(path) { playing = false }
                                playing = true
                            } else {
                                recorder.stopPlayback()
                                playing = false
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                    ) {
                        Text(if (playing) "Stop" else "Play")
                    }
                    Text("Duration: ${durationSec}s")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (bmp != null && recordingPath != null && imageUrl != null) {
                            val task = com.example.josh.data.Task(
                                id = UUID.randomUUID().toString(),
                                task_type = "image_description",
                                image_url = imageUrl,
                                image_path = null,
                                audio_path = recordingPath,
                                duration_sec = durationSec,
                                timestamp = repo.nowIso()
                            )
                            repo.saveTask(task)
                            nav.navigate(Routes.Select)
                        }
                    }
                },
                enabled = (bmp != null && recordingPath != null && imageUrl != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun PhotoCaptureScreen(nav: NavHostController, repo: TaskRepository, recorder: AudioRecorder) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    var description by remember { mutableStateOf("") }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var durationSec by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val takePreview =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bmp = bitmap
        }

    CommonScaffold(nav = nav, title = "Photo Capture", showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                "Capture any object around you and then describe it in your language.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { takePreview.launch(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Capture Image")
            }

            Spacer(Modifier.height(12.dp))

            bmp?.let {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(220.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Describe the photo in your language (text).",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                textStyle = TextStyle(fontSize = 16.sp),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Type your description here...") }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Optionally, you can record audio explanation.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(110.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                errorMsg = null
                                val start = SystemClock.elapsedRealtime()
                                val path = recorder.startRecording()
                                val released = try {
                                    awaitRelease()
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                                val out = recorder.stopRecording()
                                val elapsed =
                                    ((SystemClock.elapsedRealtime() - start) / 1000).toInt()
                                durationSec = elapsed
                                recordingPath = out
                                if (elapsed < 10) {
                                    errorMsg = "Recording too short (min 10 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                } else if (elapsed > 20) {
                                    errorMsg = "Recording too long (max 20 s)."
                                    recordingPath = null
                                    out?.let { File(it).delete() }
                                }
                            })
                        },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Text("🎙", fontSize = 28.sp)
                        Text(
                            "Optional\nHold to Record",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFC62828))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        bmp = null
                        description = ""
                        recordingPath = null
                        durationSec = 0
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Retake Photo")
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (bmp != null) {
                                val imgFile =
                                    File(context.filesDir, "photo_${UUID.randomUUID()}.jpg")
                                val fos = FileOutputStream(imgFile)
                                bmp!!.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                fos.flush()
                                fos.close()

                                val task = com.example.josh.data.Task(
                                    id = UUID.randomUUID().toString(),
                                    task_type = "photo_capture",
                                    image_path = imgFile.absolutePath,
                                    audio_path = recordingPath,
                                    duration_sec = durationSec,
                                    timestamp = repo.nowIso()
                                )
                                repo.saveTask(task)
                                nav.navigate(Routes.Select)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

@Composable
fun TaskDetailsScreen(
    nav: NavHostController,
    repo: TaskRepository,
    taskId: String?
) {
    if (taskId == null) {
        Text("Invalid task", color = Color.Red)
        return
    }

    var task by remember { mutableStateOf<com.example.josh.data.Task?>(null) }
    var playing by remember { mutableStateOf(false) }
val context = LocalContext.current
val recorder = AudioRecorder(context)


    LaunchedEffect(Unit) {
        val all = repo.loadTasks()
        task = all.find { it.id == taskId }
    }

    CommonScaffold(nav = nav, title = "Task Details", showBack = true) { padding ->

        task?.let { t ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // TITLE
                Text(
                    t.task_type.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                )

                Text("📅 ${t.timestamp}", color = Color.Gray)
                Text("⏱ Duration: ${t.duration_sec}s", color = Color.Gray)

             // IMAGE SECTION: local (photo_capture) OR remote (image_description)
when {
    // 1) Local file image (photo_capture)
    t.image_path != null -> {
        val file = File(t.image_path!!)
        if (file.exists()) {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.let { bmp ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // 2) Remote URL image (image_description)
    t.image_url != null -> {
        var bmp by remember { mutableStateOf<Bitmap?>(null) }
        val imageUrl = t.image_url

        LaunchedEffect(imageUrl) {
            try {
                bmp = withContext(Dispatchers.IO) {
                    val connection = URL(imageUrl!!).openConnection()
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    BitmapFactory.decodeStream(connection.getInputStream())
                }
            } catch (_: Exception) {}
        }

        bmp?.let { img ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            "Image URL: ${t.image_url}",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}


                // TEXT CONTENT
                t.text?.let {
                    Text(
                        "Content",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryTeal
                        )
                    )
                    Text(it, color = Color.DarkGray)
                }

// AUDIO
t.audio_path?.let { audioPath ->

    Text(
        "Audio Recording",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = PrimaryTeal
        )
    )

    Button(
        onClick = {
            if (!playing) {
                recorder.play(audioPath) {   // <-- CORRECT CALL
                    playing = false
                }
                playing = true
            } else {
                recorder.stopPlayback()
                playing = false
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealLight)
    ) {
        Text(if (playing) "Stop Audio" else "Play Audio")
    }
}


            }

        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...", color = Color.Gray)
            }
        }
    }
}


@Composable
fun TaskHistoryScreen(nav: NavHostController, repo: TaskRepository) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf<List<com.example.josh.data.Task>>(emptyList()) }

    LaunchedEffect(Unit) {
        tasks = repo.loadTasks()
    }

    CommonScaffold(nav = nav, title = "Task History", showBack = true) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            val totalDuration = tasks.sumOf { it.duration_sec }

            // ---------- TOP SUMMARY CARD ----------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Tasks", fontWeight = FontWeight.Bold)
                        Text(
                            "${tasks.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Duration", fontWeight = FontWeight.Bold)
                        Text(
                            "${totalDuration}s",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------- EMPTY STATE ----------
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks recorded yet.", color = Color.Gray)
                }
                return@Column
            }

            Text(
                "Your Records",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryTeal
            )

            Spacer(Modifier.height(12.dp))

            // ---------- LIST OF TASKS ----------
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tasks.forEach { t ->

                    Card(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { nav.navigate("${Routes.TaskDetails}/${t.id}") }
        .padding(vertical = 4.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
)
 {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // ICONS BASED ON TASK TYPE
                            val iconEmoji = when (t.task_type) {
                                "text_reading" -> "📖"
                                "image_description" -> "🖼"
                                "photo_capture" -> "📸"
                                else -> "📁"
                            }

                            Text(
                                iconEmoji,
                                fontSize = 30.sp,
                                modifier = Modifier.padding(end = 14.dp)
                            )

                            // MAIN TEXT SECTION
                            Column(modifier = Modifier.weight(1f)) {

                                Text(
                                    t.task_type.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    "Duration: ${t.duration_sec}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Text(
                                    t.timestamp ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                if (t.text != null) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        t.text.take(80) + if (t.text.length > 80) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            // IMAGE PREVIEW (if any)
                            t.image_path?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(58.dp)
                                                .padding(start = 8.dp)
                                                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
