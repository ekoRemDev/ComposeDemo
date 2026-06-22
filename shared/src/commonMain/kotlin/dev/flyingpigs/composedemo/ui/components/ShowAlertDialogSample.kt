package dev.flyingpigs.composedemo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@Composable
fun AlertDialogSample() {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        ShowAlertDialogSample(
            showDialog = showDialog.value,
            onDismiss = { showDialog.value = false })
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                showDialog.value = true
            }
        ) {
            Text(
                text = "Show Dialog",
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun ShowAlertDialogSample(
    showDialog: Boolean,
    onDismiss: () -> Unit,
) {

    if (showDialog) {
        AlertDialog(
            title = {
                Text("Alert Title", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(text = "Are you sure?")
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
    }
}