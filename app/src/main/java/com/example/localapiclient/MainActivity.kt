package com.example.localapiclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localapiclient.ui.theme.LocalApiClientTheme
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Define an enum for the view modes
enum class ViewMode {
    CRITICA, // For asking AI questions
    VIDEOS   // For displaying video lists
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalApiClientTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreenWithNavigationDrawer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNavigationDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }

    // --- Hoisted State ---
    var videoResults by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var answer by remember { mutableStateOf("") }
    var bestCar by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingCritica by remember { mutableStateOf(false) }
    var isLoadingVideos by remember { mutableStateOf(false) }
    var videoKeyword by remember { mutableStateOf("Tsuru") } // State for the video keyword input

    var currentViewMode by remember { mutableStateOf(ViewMode.CRITICA) }
    val coroutineScope = rememberCoroutineScope()

    // Extracted video fetching logic to be accessible in MainScreenWithNavigationDrawer
    fun fetchVideos(keyword: String, setIsLoading: (Boolean) -> Unit, setResults: (Map<String,String>) -> Unit) {
        coroutineScope.launch {
            try {
                setResults(emptyMap()) // Clear previous before fetching
                setIsLoading(true)
                Log.d("MainActivity", "Fetching videos for keyword: $keyword")
                val call = RetrofitClient.videoApi.getVideos(
                    channel_id = "UC-kBlBK4icUzAN-2amwIRQA", // Example
                    keyword = keyword
                )

                call.enqueue(object : Callback<VideosResponse> {
                    override fun onResponse(
                        call: Call<VideosResponse>,
                        response: Response<VideosResponse>
                    ) {
                        setIsLoading(false)
                        if (response.isSuccessful) {
                            val videosResponseData = response.body()
                            setResults(videosResponseData?.results ?: emptyMap())
                            Log.d("MainActivity", "Videos fetched. Count: ${videosResponseData?.results?.size}")
                        } else {
                            Log.e("MainActivity", "Error fetching videos: ${response.code()} - ${response.message()}")
                            setResults(emptyMap())
                        }
                    }

                    override fun onFailure(call: Call<VideosResponse>, t: Throwable) {
                        setIsLoading(false)
                        Log.e("MainActivity", "Exception fetching videos", t)
                        setResults(emptyMap())
                    }
                })
            } catch (e: Exception) {
                setIsLoading(false)
                Log.e("MainActivity", "Exception initiating video fetch", e)
                setResults(emptyMap())
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Critica de automovil"
                        )
                    },
                    label = { Text("Critica de automovil") },
                    selected = currentViewMode == ViewMode.CRITICA,
                    onClick = {
                        currentViewMode = ViewMode.CRITICA
                        videoResults = emptyMap() // Clear video results
                        // Optionally clear AI results if you want a fresh screen each time
                        // answer = ""
                        // bestCar = ""
                        // imageUrl = null
                        // isLoadingCritica = false
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Videos de automovil"
                        )
                    },
                    label = { Text("Videos de automovil") },
                    selected = currentViewMode == ViewMode.VIDEOS,
                    onClick = {
                        currentViewMode = ViewMode.VIDEOS
                        // Clear AI question results
                        answer = ""
                        bestCar = ""
                        imageUrl = null
                        isLoadingCritica = false
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Handle "Settings" click
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = "Acerca") },
                    label = { Text("Acerca") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentViewMode) {
                                ViewMode.CRITICA -> "Critica de Automovil"
                                ViewMode.VIDEOS -> "Videos de Automovil"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                            Icon(Icons.Filled.Menu, "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            ApiUI(
                modifier = Modifier.padding(innerPadding),
                currentViewMode = currentViewMode,
                videoResults = videoResults,
                answer = answer,
                onAnswerChange = { answer = it },
                bestCar = bestCar,
                onBestCarChange = { bestCar = it },
                imageUrl = imageUrl,
                onImageUrlChange = { imageUrl = it },
                isLoadingCritica = isLoadingCritica,
                onIsLoadingCriticaChange = { isLoadingCritica = it },
                isLoadingVideos = isLoadingVideos,
                videoKeyword = videoKeyword,
                onVideoKeywordChange = { videoKeyword = it },
                onFetchVideos = { keyword -> fetchVideos(keyword, { isLoadingVideos = it }, { videoResults = it }) } // Now fetchVideos is in scope

            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("Acerca de ...") },
                text = { Text("Tu Coche Reviews v1.0") },
                confirmButton = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { showAboutDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) { Text("Cerrar") }
                    }
                },
                icon = { Icon(Icons.Filled.Person, contentDescription = "About Icon") }
            )
        }
    }
}

@Composable
fun ApiUI(
    modifier: Modifier = Modifier,
    currentViewMode: ViewMode,
    videoResults: Map<String,String>,
    answer: String,
    onAnswerChange: (String) -> Unit,
    bestCar: String,
    onBestCarChange: (String) -> Unit,
    imageUrl: String?,
    onImageUrlChange: (String?) -> Unit,
    isLoadingCritica: Boolean,
    onIsLoadingCriticaChange: (Boolean) -> Unit,
    isLoadingVideos: Boolean,
    videoKeyword: String,
    onVideoKeywordChange: (String) -> Unit,
    onFetchVideos: (String) -> Unit
) {
    var question by remember { mutableStateOf("Cual es el mejor carro practico") } // Stays local to ApiUI if only used for this input
    val coroutineScope = rememberCoroutineScope() // Hoist coroutineScope to ApiUI level
    val scrollState = rememberScrollState()
    val localContext = LocalContext.current


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp), // Content padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentViewMode) {
            ViewMode.CRITICA -> {
                // --- UI for "Critica de automovil" ---
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    textStyle = TextStyle(fontSize = 14.sp),
                    label = { Text(text = "Haz tu pregunta ", fontStyle = FontStyle.Italic) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onIsLoadingCriticaChange(true)
                        coroutineScope.launch {
                            val request = QuestionInput(question)
                            RetrofitClient.api.askQuestion(request)
                                .enqueue(object : Callback<QuestionResponse> {
                                    override fun onResponse(
                                        call: Call<QuestionResponse>,
                                        response: Response<QuestionResponse>
                                    ) {
                                        onIsLoadingCriticaChange(false)
                                        val data = response.body()
                                        if (response.isSuccessful && data != null) {
                                            onAnswerChange(data.answer ?: "")
                                            onBestCarChange(data.best_car ?: "")
                                            onImageUrlChange(data.image_url)
                                        } else {
                                            onAnswerChange("Error: ${response.code()} - ${response.message()}")
                                            onBestCarChange("")
                                            onImageUrlChange(null)
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<QuestionResponse>,
                                        t: Throwable
                                    ) {
                                        onIsLoadingCriticaChange(false)
                                        onAnswerChange("Failure: ${t.message}")
                                        onBestCarChange("")
                                        onImageUrlChange(null)
                                    }
                                })
                        }
                    },
                    enabled = !isLoadingCritica,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Preguntale a IA")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingCritica) {
                    CircularProgressIndicator(color = Color.Red)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (answer.isNotEmpty()) {
                            Text(text = "Respuesta: ", fontWeight = FontWeight.Bold)
                            val annotatedString = buildAnnotatedString {
                                append(answer)
                                answer.split(" ").forEach { word ->
                                    if (word.startsWith("@") || word.startsWith("(@")) {
                                        val urlTarget = word.trimStart('(', '@')
                                            .removeSuffix(")") // Handle cases like (@user)
                                        try {
                                            val annotationStart = answer.indexOf(word)
                                            val annotationEnd = annotationStart + word.length
                                            if (annotationStart != -1) { // Ensure word is found
                                                addStringAnnotation(
                                                    "URL",
                                                    "https://x.com/$urlTarget",
                                                    annotationStart,
                                                    annotationEnd
                                                )
                                                addStyle(
                                                    style = SpanStyle(
                                                        color = Color.Blue,
                                                        textDecoration = TextDecoration.Underline
                                                    ),
                                                    annotationStart,
                                                    annotationEnd
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e(
                                                "ApiUI",
                                                "Error creating annotation for word: $word",
                                                e
                                            )
                                        }
                                    }
                                }
                            }
                            ClickableText(text = annotatedString, onClick = { offset ->
                                annotatedString.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        try {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(annotation.item)
                                            )
                                            localContext.startActivity(intent)
                                        } catch (e: Exception) {
                                            Log.e(
                                                "ApiUI",
                                                "Could not open URL: ${annotation.item}",
                                                e
                                            )
                                        }
                                    }
                            })
                        } else {
                            if (bestCar.isNotBlank() || imageUrl != null) { // Show message if there was a previous non-empty response
                                Text(text = "Respuesta: (No disponible o vacía)")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (bestCar.isNotEmpty()) {
                            Text(
                                text = "Carro que mas se adapta a tu pregunta: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bestCar,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            if (answer.isNotBlank() || imageUrl != null) {
                                Text(text = "Carro que mas se adapta a tu pregunta: (No disponible)")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        imageUrl?.let { url ->
                            if (url.isNotBlank()) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Image for $bestCar",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp) // Added fixed height for example
                                )
                            }
                        }
                    }
                }
            }

            ViewMode.VIDEOS -> {
                // --- UI for "Videos de automovil" ---
                OutlinedTextField(
                    value = videoKeyword,
                    onValueChange = onVideoKeywordChange,
                    label = { Text("Palabra clave del video") },
                    leadingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Video Keyword Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (videoKeyword.isNotBlank()) {
                            Log.d("MainActivity", "Button clicked with keyword: $videoKeyword")
                            onFetchVideos(videoKeyword) // Call the passed lambda
                        }
                    },
                    enabled = !isLoadingVideos && videoKeyword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue, // Different color for distinction
                        contentColor = Color.White
                    )
                ) {
                    Text("Buscar Videos")
                }


                if (isLoadingVideos) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.Blue)
                } else if (videoResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Resultados de Videos:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    videoResults.forEach { (title, url) ->
                        val annotatedVideoString = buildAnnotatedString {
                            append(title) // Display the title (key)
                            if (URLUtil.isValidUrl(url)) { // Check if the URL (value) is valid
                                addStringAnnotation("VIDEO_URL", url, 0, title.length) // Annotate with the URL
                                addStyle(
                                    style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                                    0,
                                    title.length
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ClickableText(
                            text = annotatedVideoString,
                            onClick = { offset ->
                                annotatedVideoString.getStringAnnotations("VIDEO_URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        localContext.startActivity(intent)
                                    }
                            }
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No se encontraron videos o no se han buscado.")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LocalApiClientTheme {
        MainScreenWithNavigationDrawer()
    }
}