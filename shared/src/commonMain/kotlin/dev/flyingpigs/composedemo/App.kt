package dev.flyingpigs.composedemo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var submittedName by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        Scaffold(
            topBar = { AppTopBar(
            ) },
            containerColor = Color(red = 187, green = 134, blue = 252),
            content = {
                innerPadding ->
                Surface(
                    color = Color(red = 187, green = 134, blue = 252),
                    modifier = Modifier.fillMaxSize()) {

                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize().pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Logo()
                        Text("Hello $submittedName")
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
                    }
                }
            },
        )

    }
}
