package dev.flyingpigs.composedemo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun AppFloatingActionButton(isVisible: Boolean, onToggle: () -> Unit) {
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        ShowAlertDialogSample(
            showDialog = showDialog.value, onDismiss = {
                showDialog.value = false
            })
    }
    FloatingActionButton(
        containerColor = if(isVisible){
            Color.White
        } else {
            Color.Red
        },
        onClick = {
            println("FAB clicked!")
            onToggle()
            val sizeOfTheList = randomTaskButton()

//            if (sizeOfTheList == 0) {
//                showDialog.value = false
//            } else {
//                showDialog.value = true
//            }
        }) {
        // Icon or text content inside the FAB
        Icon(
            imageVector = Icons.Default.Home, contentDescription = "Add Item"
        )
    }
}