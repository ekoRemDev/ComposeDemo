package dev.flyingpigs.composedemo.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Confirmation dialog shown before logging out.
 *
 * Stateless: the caller owns the "is it visible?" flag and decides what
 * confirm/dismiss do. Tapping "Yes" calls [onConfirm], "No" or tapping
 * outside calls [onDismiss].
 */
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {

    AlertDialog(
        title = {
            LogoutTitle()
        },
        text = {
            Text("Are you sure you want to log out?")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        },
    )
}

@Composable
fun LogoutTitle() {
    Text("Log out", style = MaterialTheme.typography.titleLarge)
}