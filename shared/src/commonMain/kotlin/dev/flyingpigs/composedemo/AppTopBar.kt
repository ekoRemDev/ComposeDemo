package dev.flyingpigs.composedemo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun goBackToPreviousScreen() {
    TODO("Not yet implemented")
}

@Composable
fun AppTopBar() {
    TopAppBar(title = { Text("Composedemo") }, navigationIcon = {
        IconButton(onClick = { goBackToPreviousScreen() }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White
            )
        }
    })
}