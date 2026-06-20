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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mars.ui.theme.MARSTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spokenText =
                result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                handleCommand(spokenText)
            } else {
                speak("I did not hear anything clearly.")
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startListening()
            } else {
                speak("Microphone permission is required for voice recognition.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        enableEdgeToEdge()

        setContent {
            MARSTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MarsHomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onTalkClick = {
                            speak("Listening.")
                            checkMicrophonePermissionAndListen()
                        },
                        onCameraClick = {
                            speak("Camera system is not online yet.")
                        },
                        onAlertClick = {
                            speak("Test alert activated. This is only a system test.")
                        },
                        onSettingsClick = {
                            speak("Settings will be available in a future update.")
                        }
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

    private fun handleCommand(spokenText: String) {
        val command = spokenText.lowercase(Locale.UK)

        val response = when {
            command.contains("hello") || command.contains("hi mars") -> {
                "Hello Robert. MARS Mobile is online."
            }

            command.contains("who are you") || command.contains("what are you") -> {
                "I am MARS. Monitoring and Autonomous Response System."
            }

            command.contains("status") || command.contains("status report") -> {
                "Status report. Voice system online. Mobile interface online. Rover system not connected."
            }

            command.contains("time") -> {
                val time = SimpleDateFormat("h:mm a", Locale.UK).format(Date())
                "The current time is $time."
            }

            command.contains("help") -> {
                "Available commands are: hello MARS, who are you, status report, what time is it, and help."
            }

            else -> {
                "I heard: $spokenText. I do not yet know how to respond."
            }
        }

        speak(response)
    }

    private fun checkMicrophonePermissionAndListen() {
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to MARS")
        }

        speechLauncher.launch(intent)
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "MARS_SPEECH"
            )
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
    onTalkClick: () -> Unit,
    onCameraClick: () -> Unit,
    onAlertClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MARS",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Monitoring and Autonomous Response System"
        )

        Text(
            text = "Status: Command System Online"
        )

        Button(onClick = onTalkClick) {
            Text("Talk to MARS")
        }

        Button(onClick = onCameraClick) {
            Text("Camera")
        }

        Button(onClick = onAlertClick) {
            Text("Test Alert")
        }

        Button(onClick = onSettingsClick) {
            Text("Settings")
        }

        Text(
            text = "MARS Mobile v0.5"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MarsPreview() {
    MARSTheme {
        MarsHomeScreen(
            onTalkClick = {},
            onCameraClick = {},
            onAlertClick = {},
            onSettingsClick = {}
        )
    }
}
