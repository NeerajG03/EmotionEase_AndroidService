package com.example.emotionease

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import com.example.emotionease.ui.theme.EmotionEaseTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.util.concurrent.ScheduledThreadPoolExecutor


class MainActivity : ComponentActivity() {
    private lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        context = this // initialize context here
        setContent {
            EmotionEaseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    MainPage(logo = painterResource(id = R.drawable.logo),context = context)
                }
            }
        }
    }
}

fun findsarc(result : List<Category>): String{
    if (result[0].score > result[1].score){
        Log.d("Sarcasm result","Not sarcasm: "+result[0].score.toString())
        return "Not sarcasm: "+result[0].score.toString()
    }
    else{
        Log.d("Sarcasm result","sarcasm: "+result[1].score.toString())
        return "Sarcasm: "+result[1].score.toString()
    }
}

fun findemo(result : List<Category>): String{
    val values = listOf(result[0].score, result[1].score,result[2].score, result[3].score,result[4].score)
    val values_cat = listOf("Anger","Joy","Neutral","Sadness","Surprise")
    val maxValue = values.maxOrNull()
    val maxIndex = values.indexOf(maxValue)
    val category = values_cat[maxIndex]

    Log.d("Emotion result",category+": "+maxValue)
    return category+": "+maxValue
}



@Composable
fun JetpackComposeColumn(context: Context) {

    val textV = remember{mutableStateOf("result")}
    val values = listOf("Bert-Emotion", "MobileBert-Emotion", "MobileBert-Sarcasm", "AvgWrdVec-Sarcasm")
    val selectedValue = remember { mutableStateOf(values[0]) }
    val textValue = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(true) }

    fun predict(context: Context, text : String, model : String) : String{
        var value : String = ""
        val options = NLClassifier.NLClassifierOptions.builder().build()
        val options2 = BertNLClassifier.BertNLClassifierOptions.builder().build()
        if (model == "Bert-Emotion"){
            Log.d("Chosen Model",model)
            val nlClassifier = BertNLClassifier.createFromFileAndOptions(context, "bert_emotion.tflite", options2)
            val executor = ScheduledThreadPoolExecutor(1)
            executor.execute{
                val results = nlClassifier.classify(text)
                textV.value = findemo(results)
            }
        }
        else if(model=="MobileBert-Emotion"){
            Log.d("Chosen Model",model)
            val nlClassifier = BertNLClassifier.createFromFileAndOptions(context, "mobilebert_emotion.tflite", options2)
            val executor = ScheduledThreadPoolExecutor(1)
            executor.execute{
                val results = nlClassifier.classify(text)
                textV.value = findemo(results)
            }
        }
        else if(model=="MobileBert-Sarcasm"){
            Log.d("Chosen Model",model)
            val nlClassifier = BertNLClassifier.createFromFileAndOptions(context, "mobilebert_sarcasm.tflite", options2)
            val executor = ScheduledThreadPoolExecutor(1)
            executor.execute{
                val results = nlClassifier.classify(text)
                textV.value = findsarc(results)
            }
        }
        else{
            Log.d("Chosen Model",model)
            val nlClassifier = NLClassifier.createFromFileAndOptions(context, "model.tflite", options)
            val executor = ScheduledThreadPoolExecutor(1)
            executor.execute{
                val results = nlClassifier.classify(text)
                textV.value = findsarc(results)
            }

        }
        Log.d("Predict Function",value)
        return value
    }

    // A column to arrange the components vertically
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = textV.value,
            modifier = Modifier
                .padding(top = 3.dp, bottom = 10.dp, start = 47.dp)
                .align(Alignment.Start),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
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

        OutlinedTextField(
                value = textValue.value,
                onValueChange = { textValue.value = it },
                label = { Text(text = "Enter sentence") }
            )
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                modifier = Modifier
//                    .align(Alignment.Start)
                    .padding(start = 10.dp, end = 10.dp)
            ) {
                IconButton(
                    onClick = { expanded.value = true },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Expansion")
                }
                Text(
                    text = "Models",
                    modifier = Modifier
                        .padding(top = 22.dp, bottom = 20.dp),
                )
            }
            // A button to submit the textfield value
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Button(
                    modifier = Modifier.padding(top = 10.dp, end = 20.dp),
                    onClick = {
                        Log.d("TAG", selectedValue.value + " " + textValue.value)
                        textV.value =
                            predict(context = context, textValue.value, selectedValue.value)
                        Log.d("Result", textV.value)
                    }
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun MainPage(logo: Painter,context: Context) {
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
            Image(
                painter = logo,
                contentDescription = "Logo",
                modifier = Modifier.padding(horizontal = 50.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inversePrimary)
            )
            Text(
                text = "Emotion Ease",
                modifier = Modifier
                    .padding(top = 3.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )


            JetpackComposeColumn(context = context)

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
//        MainPage(logo = painterResource(id = R.drawable.logo),)
    }
}