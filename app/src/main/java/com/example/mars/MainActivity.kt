package com.example.mars

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mars.ui.theme.MARSTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private var lastHeard by mutableStateOf("Awaiting command...")
    private var lastResponse by mutableStateOf("MARS standing by.")

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                lastHeard = spokenText
                handleCommand(spokenText)
            } else {
                respond("I did not hear anything clearly.")
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
            else respond("Microphone permission is required.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)
        enableEdgeToEdge()

        setContent {
            MARSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MarsHomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        lastHeard = lastHeard,
                        lastResponse = lastResponse,
                        onTalkClick = { checkPermissionAndListen() },
                        onCameraClick = { respond("Camera system is not online yet.") },
                        onAlertClick = { respond("Test alert activated. This is only a system test.") },
                        onSettingsClick = { respond("Settings will be available in a future update.") }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK
            ttsReady = true
        }
    }

    private fun checkPermissionAndListen() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) startListening()
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to MARS")
        }

        speechLauncher.launch(intent)
    }

    private fun handleCommand(commandText: String) {
        val command = commandText.lowercase(Locale.UK)

        val response = when {
            command.contains("hello") || command.contains("hi mars") ->
                "Hello Christian. MARS Mobile is online."

            command.contains("who are you") || command.contains("what are you") ->
                "I am MARS. Monitoring and Autonomous Response System."

            command.contains("status") ->
                "Status report. Voice system online. Mobile interface online."

            command.contains("time") -> {
                val currentTime = SimpleDateFormat("h:mm a", Locale.UK).format(Date())
                "The current time is $currentTime."
            }

            command.contains("christian") ->
                "Christian is my primary user."

            command.contains("help") ->
                "Say hello MARS, who are you, status report, or what time is it."

            else ->
                "I heard $commandText, but I do not understand that command yet."
        }

        respond(response)
    }

    private fun respond(text: String) {
        lastResponse = text
        speak(text)
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MARS_SPEECH")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun MarsHomeScreen(
    modifier: Modifier = Modifier,
    lastHeard: String,
    lastResponse: String,
    onTalkClick: () -> Unit,
    onCameraClick: () -> Unit,
    onAlertClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MARS", style = MaterialTheme.typography.headlineLarge)
        Text("Monitoring and Autonomous Response System")
        Text("Primary User: Christian")
        Text("Status: Online")

        Text("Heard: $lastHeard")
        Text("Reply: $lastResponse")

        Button(onClick = onTalkClick, modifier = Modifier.fillMaxWidth()) {
            Text("Talk to MARS")
        }

        Button(onClick = onCameraClick, modifier = Modifier.fillMaxWidth()) {
            Text("Camera")
        }

        Button(onClick = onAlertClick, modifier = Modifier.fillMaxWidth()) {
            Text("Test Alert")
        }

        Button(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }

        Text("MARS Mobile v0.6 Light")
    }
}

@Preview(showBackground = true)
@Composable
fun MarsPreview() {
    MARSTheme {
        MarsHomeScreen(
            lastHeard = "Hello MARS",
            lastResponse = "Hello Christian. MARS Mobile is online.",
            onTalkClick = {},
            onCameraClick = {},
            onAlertClick = {},
            onSettingsClick = {}
        )
    }
}