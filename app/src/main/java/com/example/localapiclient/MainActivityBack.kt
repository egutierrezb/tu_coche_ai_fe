package com.example.localapiclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.localapiclient.ui.theme.LocalApiClientTheme
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ... other imports ...


class MainActivityBack : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalApiClientTheme {
                // Surface is still good as a root for theming
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreenWithNavigationDrawer2() // New entry point
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and other Material 3 components
@Composable
fun MainScreenWithNavigationDrawer2() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var videoResults by remember { mutableStateOf<Map<String,String>>(emptyMap()) } // Changed to List<VideoItem>
    //This is the actual slide-in menu providing access to different sections of the app
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Content of your Navigation Drawer
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Crítica de automovil") },
                    label = { Text("De qué automóvil buscas su crítica?") },
                    selected = false, // You can manage selection state if needed
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Handle "Search Car" click, e.g., navigate or change UI state
                        // For this example, we'll just close the drawer
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Videos de automovil") },
                    label = { Text("De qué automóvil buscas su video?") },
                    selected = false, // You can manage selection state if needed
                    onClick = {
                        scope.launch {
                            try {
                                val call = RetrofitClient.videoApi.getVideos(
                                    channel_id = "UC-kBlBK4icUzAN-2amwIRQA",
                                    keyword = "Tsuru"
                                )
                                call.enqueue(object : Callback<VideosResponse> {
                                    override fun onResponse(call: Call<VideosResponse>, response: Response<VideosResponse>) {
                                        if (response.isSuccessful && response.body() != null) {
                                            videoResults = response.body()?.results?: emptyMap<String, String>()
                                            Log.d("MainActivity","Response: $response")
                                            Log.d("MainActivity", "Videos fetched: ${videoResults.size}")
                                            videoResults.forEach { video ->
                                                Log.d("MainActivity", "Video Title: ${video.key}")
                                            }
                                        } else {
                                            Log.e(
                                                "MainActivity",
                                                "Error fetching videos: ${response.code()} - ${response.message()}"
                                            )
                                            videoResults = emptyMap()
                                        }
                                    }

                                    override fun onFailure(call: Call<VideosResponse>, t: Throwable) {
                                        Log.e("MainActivity", "Exception fetching videos", t)
                                        videoResults = emptyMap()
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Exception fetching videos", e)
                                videoResults = emptyMap()
                            }
                            drawerState.close()
                        }
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
                // Add more items as needed
            }
        },
        gesturesEnabled = drawerState.isOpen // Optionally disable swipe gestures when open
    ) {
        // Main content area
        Box { // Use Box to overlay AlertDialog
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tu Coche Reviews!") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                                Icon(Icons.Filled.Menu, "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding -> ApiUI(modifier = Modifier.padding(innerPadding), videoResults = videoResults) }

        // Your main screen content (ApiUI) goes here, within a Scaffold
        //Layout component for building user interfaces, it acts as a container that organizes
        // and arranges common UI elements such as navigation drawers
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tu Coche! Reviews") },
                    navigationIcon = {
                        //It contains an icon in order to display the drawerState
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Navigation Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors( // Optional: Customize colors
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            // Pass the padding to ApiUI so content isn't obscured by the TopAppBar
            ApiUI(modifier = Modifier.padding(innerPadding), videoResults = videoResults)
        }
        }

        // AlertDialog for "About"
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("Acerca de ...") },
                text = { Text("Tu Coche Reviews v1.0") },
                confirmButton = {
                    //In order to display the button on the middle
                    //we should display it inside of a box
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { showAboutDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                },
                icon = { Icon(Icons.Filled.Person, contentDescription = "About Icon") }
            )
        }



    }
}

// Removed unused extension functions
// private fun Unit.body() { ... }
// private fun Any.getVideos(channelId: String, keyword: String) {}

@Composable
fun ApiUI(modifier: Modifier = Modifier, videoResults: Map<String, String> = emptyMap()) { // Accept a Modifier and videoResults
    var question by remember { mutableStateOf("Cual es el mejor carro practico") }
    var answer by remember { mutableStateOf("") }
    var bestCar by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    // var showMenu by remember { mutableStateOf(false) } // Not needed for DropdownMenu anymore

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        // Apply the passed modifier here, which includes padding from Scaffold
        // and also your existing padding and scroll behavior.
        modifier = modifier // This 'modifier' now comes from the Scaffold's content lambda
            .fillMaxSize()
            // The padding for status bars and screen edges is now handled by Scaffold + innerPadding,
            // but you might want to add some internal padding within the content area.
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // Keep horizontal/bottom padding
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The Row with "Tu Coche!" and the menu icon is now part of the TopAppBar in Scaffold
        // So we remove it from here.

        Spacer(modifier = Modifier.height(8.dp)) // Maybe adjust or remove depending on TopAppBar
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            textStyle = TextStyle(fontSize = 14.sp),
            label = { Text(text = "Haz tu pregunta ", fontStyle = FontStyle.Italic) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    val request = QuestionInput(question)
                    RetrofitClient.api.askQuestion(request).enqueue(object :
                        Callback<QuestionResponse> {
                        override fun onResponse(
                            call: Call<QuestionResponse>,
                            response: Response<QuestionResponse>
                        ) {
                            isLoading = false
                            val data = response.body()
                            if (response.isSuccessful && data != null) {
                                answer = data.answer ?: ""
                                if (answer.contains("@") || answer.startsWith("(@")) {
                                    val annotatedString = buildAnnotatedString {

                                        append(answer)
                                        answer.split(" ").forEach { word ->
                                            if (word.startsWith("@")) {
                                                addStringAnnotation("URL", "https://x.com/${word.substring(1)}", answer.indexOf(word) , answer.indexOf(word) + word.length)
                                            }
                                        }
                                    }

                                }
                                bestCar = data.best_car ?: ""
                                imageUrl = data.image_url
                            } else {
                                answer = "Error: ${response.code()} - ${response.message()}"
                                bestCar = ""
                                imageUrl = null
                            }
                        }

                        override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                            isLoading = false
                            answer = "Failure: ${t.message}"
                            bestCar = ""
                            imageUrl = null
                        }
                    })
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Preguntale a IA")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.Red)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(), // Removed weight and modifier, parent Column handles scrolling
                        horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (answer.isNotEmpty()) {
                    Text(text = "Respuesta: ", fontWeight = FontWeight.Bold)
                    val annotatedString = buildAnnotatedString {
                        append(answer)
                        answer.split(" ").forEach { word ->
                            if (word.startsWith("@") || word.startsWith("(@")) {
                                Log.i("MainActivity", "Found word starting with @: $word")
                                addStringAnnotation("URL", "https://x.com/${word.substring(1)}", answer.indexOf(word), answer.indexOf(word) + word.length)
                                addStyle(style = SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                ), answer.indexOf(word), answer.indexOf(word) + word.length)
                            }
                        }
                    }
                    Log.i("MainActivity","annotatedString: $annotatedString")
                    val localContext = LocalContext.current
                    ClickableText(text = annotatedString, onClick = { offset ->
                        annotatedString.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                localContext.startActivity(intent)
                            }
                    })
                } else {
                    if (answer.isNotBlank() || bestCar.isNotBlank()) {
                        Text(text = "Respuesta: (Not available or empty)")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (bestCar.isNotEmpty()) {
                    Text(text = "Carro que mas se adapta a tu pregunta: ", fontWeight = FontWeight.Bold)
                    Text(text = bestCar, color = Color.Blue, fontWeight = FontWeight.Bold)
                } else {
                    if (answer.isNotBlank() || bestCar.isNotBlank()) {
                        Text(text = "Carro que mas se adapta a tu pregunta: (No disponible)")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                imageUrl?.let { url ->
                    if (url.isNotBlank()) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Image for $bestCar",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Display Video Results
        if (videoResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Resultados de Videos:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            val localContext = LocalContext.current
            videoResults.forEach { (title, url) ->
                Spacer(modifier = Modifier.height(8.dp))
                val annotatedString = buildAnnotatedString {
                    append("Título: ")
                    pushStringAnnotation(tag = "URL", annotation = url) // Assume videoItem is the URL
                    withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                        append(title)
                    }
                    pop()
                }
                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                localContext.startActivity(intent)
                            }
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var str = "Juan"+"1"
    Text(
        text = "Hello "+str,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun ApiUIPreview() {
    LocalApiClientTheme {
        ApiUI()
    }
}