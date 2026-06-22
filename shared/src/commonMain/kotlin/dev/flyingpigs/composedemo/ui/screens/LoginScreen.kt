package dev.flyingpigs.composedemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(red = 187, green = 134, blue = 252))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ){
        Text(text = "Login Screen", modifier = Modifier.padding(16.dp))

        Button(
            onClick = onContinue,
        ) {
            Text("Login")
        }
    }
}