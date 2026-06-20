package com.example.mars

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MARSTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    MarsHomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MarsHomeScreen(
    modifier: Modifier = Modifier
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

        Button(
            onClick = { }
        ) {
            Text("Talk")
        }

        Button(
            onClick = { }
        ) {
            Text("Rover")
        }

        Button(
            onClick = { }
        ) {
            Text("Camera")
        }

        Button(
            onClick = { }
        ) {
            Text("Settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarsPreview() {
    MARSTheme {
        MarsHomeScreen()
    }
}

