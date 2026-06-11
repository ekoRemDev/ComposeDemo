package dev.flyingpigs.composedemo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


@Composable
fun AppTopBar() {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ShowAlertDialogSample(
            showDialog = showDialog,
            onDismiss = { showDialog = false }
        )
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Yellow, scrolledContainerColor = Color.Cyan
    ), title = { Text("Composedemo") }, navigationIcon = {
        IconButton(onClick = { showDialog = true }) {
            Icon(
                Icons.Default.Edit, contentDescription = null, tint = Color.Green
            )
        }
    })
}