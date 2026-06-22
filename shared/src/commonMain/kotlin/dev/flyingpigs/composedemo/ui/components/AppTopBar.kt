package dev.flyingpigs.composedemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun AppTopBar(onLogout: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LogoutDialog(
            onConfirm = {
                showDialog = false
                onLogout()
            },
            onDismiss = { showDialog = false },
        )
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.DarkGray, scrolledContainerColor = Color.Cyan
    ),
        title = {
            Text(
                "Composedemo", modifier = Modifier.background(
                    Color.Green.copy(alpha = 0.2f)
                ).clickable(onClick = {
                    showDialog = true
                })
            )
        }, modifier = Modifier.background(Color.Red).padding(all = 1.dp), navigationIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.Home, contentDescription = null, tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.ExitToApp, contentDescription = null, tint = Color.White
                )
            }
        }
    )
}