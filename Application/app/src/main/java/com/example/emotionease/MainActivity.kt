package com.example.emotionease

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import com.example.emotionease.ui.theme.EmotionEaseTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emotionease.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.concurrent.ScheduledThreadPoolExecutor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmotionEaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    MainPage(logo = painterResource(id = R.drawable.logo))
                }
            }
        }
    }
}
@Composable
fun JetpackComposeColumn() {
    val values = listOf("Bert-Emotion", "MobileBert-Emotion", "MobileBert-Sarcasm", "AvgWrdVec-Sarcasm")
    val selectedValue = remember { mutableStateOf(values[0]) }
    val textValue = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(true) }

    // A column to arrange the components vertically
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // A dropdown menu to select a value from the list
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = { selectedValue.value = value
                        expanded.value = false },
                    )
            }
        }

        // A textfield to enter some text
        Row {
            OutlinedTextField(
                value = textValue.value,
                onValueChange = { textValue.value = it },
                label = { Text(text = "Enter some text") }
            )
            IconButton(
                onClick = { expanded.value = true },
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Expansion")
            }
        }
        // A button to submit the textfield value
        Button(onClick = {
            Log.d("TAG", selectedValue.value + " "  + textValue.value)
            // Do something with the textfield value and the selected value
        }) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun MainPage(logo: Painter) {
    val values = listOf("Value 1", "Value 2", "Value 3", "Value 4")
    val selectedValue = remember { mutableStateOf(values[0]) }
    val textValue = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    var showSecondPage by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {
//            Image(
//                painter = logo,
//                contentDescription = "Logo",
//                modifier = Modifier.padding(horizontal = 50.dp),
//                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inversePrimary)
//            )

            JetpackComposeColumn()
            Text(
                text = "Emotion Ease",
                modifier = Modifier
                    .padding(top = 3.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showSecondPage = true },

            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
        if (showSecondPage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                SecondPage(
                    onClose = { showSecondPage = false },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondPage(onClose: () -> Unit,modifier: Modifier = Modifier) {
    var textQuery by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    var sarcasm by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .wrapContentSize()
            .padding(10.dp),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                OutlinedTextField(
                    value = textQuery,
                    onValueChange = { textQuery = it },
                    label = { Text("Sentence") }
                )
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Expansion")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Happy") },
                        onClick = { emotion = "Happy" },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null
                            )
                        })
                    DropdownMenuItem(
                        text = { Text("Sad") },
                        onClick = { emotion = "Sad" },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = null
                            )
                        })
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Unsure but classified wrong!") },
                        onClick = { emotion = "Bad Inference" },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null
                            )
                        },
                        trailingIcon = { Text("", textAlign = TextAlign.Center) })
                }
            }


            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    ){
                Checkbox(
                    checked = sarcasm,
                    onCheckedChange = { sarcasm = it },
//                    modifier = Modifier.padding()
                )
                Text(
                    text = "Sarcastic",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)

                )
            }
            Row() {
                Button(onClick = onClose,
                    modifier = Modifier.padding(10.dp)) {
                    Text("Submit")
                }
                Button(onClick = onClose,
                    Modifier.padding(10.dp)) {
                    Text("Close")
                }
            }

        }
    }
}
val emotions = listOf("Happy", "Sad", "Angry", "Neutral")


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EmotionEaseTheme {
        MainPage(logo = painterResource(id = R.drawable.logo))
    }
}