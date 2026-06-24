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

    private var currentUser by mutableStateOf("Unknown")
    private var lastHeard by mutableStateOf("Awaiting command...")
    private var lastResponse by mutableStateOf("MARS standing by.")

    private var selectingUser = false

    private val memories = mutableMapOf(
        "Christian" to mutableListOf<String>(),
        "Anne" to mutableListOf<String>(),
        "Finlay" to mutableListOf<String>(),
        "Guest" to mutableListOf<String>()
    )

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                lastHeard = spokenText

                if (selectingUser) {
                    handleUserSelection(spokenText)
                } else {
                    handleCommand(spokenText)
                }
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
                        currentUser = currentUser,
                        lastHeard = lastHeard,
                        lastResponse = lastResponse,
                        onTalkClick = {
                            if (currentUser == "Unknown") {
                                selectingUser = true
                                respond("Who am I speaking to?")
                                checkPermissionAndListen()
                            } else {
                                checkPermissionAndListen()
                            }
                        },
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

    private fun handleUserSelection(spokenText: String) {
        val name = spokenText.lowercase(Locale.UK)

        when {
            name.contains("christian") -> {
                currentUser = "Christian"
                selectingUser = false
                respond("Hello Christian. How can I assist?")
            }

            name.contains("anne") -> {
                currentUser = "Anne"
                selectingUser = false
                respond("Hello Anne. How can I assist?")
            }

            name.contains("finlay") -> {
                currentUser = "Finlay"
                selectingUser = false
                respond("Hello Finlay. How can I assist?")
            }

            name.contains("guest") -> {
                currentUser = "Guest"
                selectingUser = false
                respond("Hello Guest. How can I assist?")
            }

            else -> {
                selectingUser = true
                respond("I did not recognise that name. Please say Christian, Anne, Finlay, or Guest.")
                checkPermissionAndListen()
            }
        }
    }

    private fun handleCommand(commandText: String) {
        val command = commandText.lowercase(Locale.UK)

        val response = when {
            command.startsWith("remember that") -> {
                rememberFact(commandText)
            }

            command.contains("what do you know about") -> {
                recallMemory(commandText)
            }

            command.contains("what do you remember about me") -> {
                recallMemoryForUser(currentUser)
            }

            command.contains("change user") ||
                    command.contains("identify user") ||
                    command.contains("who am i speaking to") -> {
                selectingUser = true
                "Who am I speaking to?"
            }

            command.contains("hello") || command.contains("hi mars") ->
                "Hello $currentUser. MARS Mobile is online."

            command.contains("who are you") || command.contains("what are you") ->
                "I am MARS. Monitoring and Autonomous Response System."

            command.contains("status") ->
                "Status report. Voice system online. Mobile interface online. Memory system online."

            command.contains("time") -> {
                val currentTime = SimpleDateFormat("h:mm a", Locale.UK).format(Date())
                "The current time is $currentTime."
            }

            command.contains("who am i") || command.contains("who is speaking") ->
                "You are currently identified as $currentUser."

            command.contains("help") ->
                "Say hello MARS, who are you, status report, what time is it, who am I, change user, remember that, or what do you know about."

            else ->
                "I heard $commandText, but I do not understand that command yet."
        }

        respond(response)

        if (selectingUser) {
            checkPermissionAndListen()
        }
    }

    private fun rememberFact(commandText: String): String {
        val fact = commandText
            .replace("Remember that", "", ignoreCase = true)
            .trim()

        if (fact.isBlank()) {
            return "I need something to remember."
        }

        val targetUser = when {
            fact.lowercase(Locale.UK).startsWith("christian") -> "Christian"
            fact.lowercase(Locale.UK).startsWith("anne") -> "Anne"
            fact.lowercase(Locale.UK).startsWith("finlay") -> "Finlay"
            fact.lowercase(Locale.UK).startsWith("guest") -> "Guest"
            fact.lowercase(Locale.UK).startsWith("i ") -> currentUser
            fact.lowercase(Locale.UK).startsWith("my ") -> currentUser
            else -> currentUser
        }

        if (targetUser == "Unknown") {
            return "I need to know who I am speaking to before I can remember that."
        }

        memories[targetUser]?.add(fact)

        return "I will remember that $fact."
    }

    private fun recallMemory(commandText: String): String {
        val lower = commandText.lowercase(Locale.UK)

        val targetUser = when {
            lower.contains("christian") -> "Christian"
            lower.contains("anne") -> "Anne"
            lower.contains("finlay") -> "Finlay"
            lower.contains("guest") -> "Guest"
            lower.contains("me") -> currentUser
            else -> currentUser
        }

        return recallMemoryForUser(targetUser)
    }

    private fun recallMemoryForUser(user: String): String {
        val userMemories = memories[user]

        if (user == "Unknown") {
            return "I do not know who I am speaking to yet."
        }

        if (userMemories.isNullOrEmpty()) {
            return "I do not know anything about $user yet."
        }

        return userMemories.joinToString(". ")
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
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to MARS")
        }

        speechLauncher.launch(intent)
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
    currentUser: String,
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
        Text("Current User: $currentUser")
        Text("Status: Memory System Online")

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

        Text("MARS Mobile v0.8")
    }
}

@Preview(showBackground = true)
@Composable
fun MarsPreview() {
    MARSTheme {
        MarsHomeScreen(
            currentUser = "Christian",
            lastHeard = "Remember that Finlay likes Star Wars",
            lastResponse = "Finlay likes Star Wars",
            onTalkClick = {},
            onCameraClick = {},
            onAlertClick = {},
            onSettingsClick = {}
        )
    }
}