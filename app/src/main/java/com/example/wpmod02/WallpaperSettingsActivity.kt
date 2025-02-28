package com.example.wpmod02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.example.wpmod02.ui.theme.WPmod02Theme

class WallpaperSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WPmod02Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WallpaperSettingsScreen()
                }
            }
        }
    }
}

@Composable
fun WallpaperSettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { androidx.preference.PreferenceManager.getDefaultSharedPreferences(context.applicationContext) }
    var mode by remember { mutableStateOf(prefs.getString("mode", "default") ?: "default") }
    var waveAmplitude by remember { mutableStateOf(prefs.getFloat("waveAmplitude", 12f)) }
    var rotationAmplitude by remember { mutableStateOf(prefs.getFloat("rotationAmplitude", 0.8f)) }
    var angleFrequency by remember { mutableStateOf(prefs.getFloat("angleFrequency", 3f)) }
    var densityFactor by remember { mutableStateOf(prefs.getFloat("densityFactor", 0.8f)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Mode", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = mode == "default", onClick = {
                mode = "default"
                prefs.edit().putString("mode", "default").apply()
            })
            Text("Default")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = mode == "custom", onClick = {
                mode = "custom"
                prefs.edit().putString("mode", "custom").apply()
            })
            Text("Custom")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = mode == "random", onClick = {
                mode = "random"
                prefs.edit().putString("mode", "random").apply()
            })
            Text("Random")
        }
        Spacer(modifier = Modifier.height(16.dp))
        SliderWithLabel(
            label = "Wave Amplitude",
            value = waveAmplitude,
            onValueChange = { waveAmplitude = it; prefs.edit().putFloat("waveAmplitude", it).apply() },
            valueRange = 0f..20f,
            steps = 40,
            enabled = mode == "custom"
        )
        SliderWithLabel(
            label = "Rotation Amplitude",
            value = rotationAmplitude,
            onValueChange = { rotationAmplitude = it; prefs.edit().putFloat("rotationAmplitude", it).apply() },
            valueRange = 0f..2f,
            steps = 20,
            enabled = mode == "custom"
        )
        SliderWithLabel(
            label = "Angle Frequency",
            value = angleFrequency,
            onValueChange = { angleFrequency = it; prefs.edit().putFloat("angleFrequency", it).apply() },
            valueRange = 1f..10f,
            steps = 18,
            enabled = mode == "custom"
        )
        SliderWithLabel(
            label = "Density Factor",
            value = densityFactor,
            onValueChange = { densityFactor = it; prefs.edit().putFloat("densityFactor", it).apply() },
            valueRange = 0.1f..2f,
            steps = 19,
            enabled = mode == "custom"
        )
    }
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean
) {
    Column {
        Text(label)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled
        )
    }
}