package dev.flyingpigs.composedemo.ui.components

import dev.flyingpigs.composedemo.ui.samples.ShowAlertDialogSample

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun AppFloatingActionButton() {
    val showDialog = remember { mutableStateOf(false) }
    if(showDialog.value){
        ShowAlertDialogSample(
            showDialog = showDialog.value,
            onDismiss = { showDialog.value = false })
    }
    FloatingActionButton(
        onClick = {
            // Handle the click action here
            println("FAB clicked!")
//            showDialog.value = true

            val sizeOfTheList = randomTaskButton()

            if(sizeOfTheList == 0) {
                showDialog.value = false
            }else{
                showDialog.value = true
            }
        }
    ) {
        // Icon or text content inside the FAB
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Item"
        )
    }
}