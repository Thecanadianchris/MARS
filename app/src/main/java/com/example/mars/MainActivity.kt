package com.example.mars

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.mars.ui.theme.MARSTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

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
                            speak("Hello Robert. How can I assist?")
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
            text = "Status: Mobile Interface Online"
        )

        Button(
            onClick = onTalkClick
        ) {
            Text("Talk to MARS")
        }

        Button(
            onClick = onCameraClick
        ) {
            Text("Camera")
        }

        Button(
            onClick = onAlertClick
        ) {
            Text("Test Alert")
        }

        Button(
            onClick = onSettingsClick
        ) {
            Text("Settings")
        }

        Text(
            text = "MARS Mobile v0.3"
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

