package com.example.localapiclient

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
class MainActivity : ComponentActivity() {
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalApiClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalApiClientTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ApiUI()
                }
            }
        }
    }
}

@Composable
fun ApiUI() {
    var question by remember { mutableStateOf("Cual es el mejor carro practico") }
    var answer by remember { mutableStateOf("") }
    var bestCar by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Create a ScrollState: this is needed for Column to be scrollable
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, // Add padding for status bar
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            //.verticalScroll(scrollState), // Add this modifier to make the Column scrollable
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text("Tu Coche!")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            textStyle = TextStyle(fontSize = 14.sp), // Change font size here
            label = { Text(text = "Haz tu pregunta ", fontStyle = FontStyle.Italic) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Reduced height
        )
        Spacer(modifier = Modifier.height(8.dp))
        // To change the color of the Button, you can use the `colors` parameter.
        // ButtonDefaults.buttonColors() allows you to customize various color aspects
        // such as backgroundColor, contentColor, disabledBackgroundColor, and disabledContentColor.
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    val request = QuestionInput(question)
                    RetrofitClient.api.askQuestion(request).enqueue(object : Callback<QuestionResponse> {
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
                                bestCar = data.best_car ?: "" // Assuming JSON key is best_car
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
                containerColor = Color.Red, // Example: Change background color to Blue
                contentColor = Color.White    // Example: Change text color to White
            )
        ) {
            Text("Preguntale a IA")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.Red) // Show loading indicator
        } else {
            // This Column will contain the scrollable content
            Column(
                modifier = Modifier
                    .weight(1f) // Takes up remaining space
                    .verticalScroll(scrollState), // Makes this part scrollable
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Your existing logic for displaying answer and bestCar
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
                    // You might want to hide "Answer is empty" if no query has been made yet
                    // or if there was an error that cleared 'answer'.
                    if (answer.isNotBlank() || bestCar.isNotBlank()) { // Show only if there was some attempt
                        Text(text = "Respuesta: (Not available or empty)")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (bestCar.isNotEmpty()) {
                    Text(text = "Carro que mas se adapta a tu pregunta: ", fontWeight = FontWeight.Bold)
                    Text(text = bestCar, color = Color.Cyan, fontWeight = FontWeight.Bold)
                } else {
                    if (answer.isNotBlank() || bestCar.isNotBlank()) { // Show only if there was some attempt
                        Text(text = "Carro que mas se adapta a tu pregunta: (No disponible)")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                imageUrl?.let { url ->
                    if (url.isNotBlank()) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Image for $bestCar",
                            modifier = Modifier.fillMaxWidth() // Adjust size as needed
                        )
                    }
                }
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

/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    var str = "Juan"+"1"
    LocalApiClientTheme {
        Greeting(str)
    }
}*/

@Preview(showBackground = true)
@Composable
fun ApiUIPreview() {
    LocalApiClientTheme {
        ApiUI()
    }
}