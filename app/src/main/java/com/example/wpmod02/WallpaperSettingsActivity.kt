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
        Text("Wallpaper Mode", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Default mode checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = mode == "default",
                onCheckedChange = { checked ->
                    if (checked) {
                        mode = "default"
                        prefs.edit().putString("mode", "default").apply()
                    }
                }
            )
            Text("Default Settings", style = MaterialTheme.typography.bodyLarge)
        }
        
        // Custom mode checkbox with sliders
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = mode == "custom",
                onCheckedChange = { checked ->
                    if (checked) {
                        mode = "custom"
                        prefs.edit().putString("mode", "custom").apply()
                    }
                }
            )
            Text("Custom Settings", style = MaterialTheme.typography.bodyLarge)
        }
        
        // Only show sliders if custom mode is selected
        if (mode == "custom") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SliderWithLabel(
                        label = "Wave Amplitude",
                        value = waveAmplitude,
                        onValueChange = { waveAmplitude = it; prefs.edit().putFloat("waveAmplitude", it).apply() },
                        valueRange = 0f..20f,
                        steps = 40,
                        enabled = true
                    )
                    SliderWithLabel(
                        label = "Rotation Amplitude",
                        value = rotationAmplitude,
                        onValueChange = { rotationAmplitude = it; prefs.edit().putFloat("rotationAmplitude", it).apply() },
                        valueRange = 0f..2f,
                        steps = 20,
                        enabled = true
                    )
                    SliderWithLabel(
                        label = "Angle Frequency",
                        value = angleFrequency,
                        onValueChange = { angleFrequency = it; prefs.edit().putFloat("angleFrequency", it).apply() },
                        valueRange = 1f..10f,
                        steps = 18,
                        enabled = true
                    )
                    SliderWithLabel(
                        label = "Density Factor",
                        value = densityFactor,
                        onValueChange = { densityFactor = it; prefs.edit().putFloat("densityFactor", it).apply() },
                        valueRange = 0.1f..2f,
                        steps = 19,
                        enabled = true
                    )
                }
            }
        }
        
        // Random mode checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = mode == "random",
                onCheckedChange = { checked ->
                    if (checked) {
                        mode = "random"
                        prefs.edit().putString("mode", "random").apply()
                    }
                }
            )
            Column {
                Text("Random Settings", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Parameters change randomly every 10 seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Information card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "How to use:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Select one of the three modes above")
                Text("2. If using Custom mode, adjust the sliders to your preference")
                Text("3. Return to home screen and set as wallpaper")
                Text("4. Changes apply immediately to active wallpaper")
            }
        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text("${String.format("%.1f", value)}")
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled
        )
    }
}