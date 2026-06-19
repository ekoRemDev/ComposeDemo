package dev.flyingpigs.composedemo.ui.screens

import dev.flyingpigs.composedemo.domain.Greeting
import dev.flyingpigs.composedemo.ui.components.GreetingLabel
import dev.flyingpigs.composedemo.ui.components.Logo
import dev.flyingpigs.composedemo.ui.samples.ShowLazyRowSample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(contentVisible: Boolean) {
    var showContent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var submittedName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Logo(modifier = Modifier.size(32.dp))
            Logo(modifier = Modifier.size(64.dp))
        }

        Text("Hello $submittedName")
        if (contentVisible) AnimatedVisibility(
            visible = contentVisible,
        ) {
            Text("Content Visible")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GreetingLabel(greeting = greeting)
            }
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        Button(
            onClick = {
                submittedName = name.trim()
                showContent = true
                focusManager.clearFocus()
            },
            enabled = name.trim().length >= 2,
        ) {
            Text("Click me!")
        }
        ShowLazyRowSample()
    }
}
