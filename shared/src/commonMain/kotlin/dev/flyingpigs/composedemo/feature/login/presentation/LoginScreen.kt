package dev.flyingpigs.composedemo.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onContinue: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state = viewModel.uiState
    var passwordVisibility by remember { mutableStateOf(false) }

    // One-shot side effect: navigate once when login succeeds.
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            onContinue()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(red = 187, green = 134, blue = 252)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Login Screen", modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.onEvent(LoginEvent.UsernameChanged(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            trailingIcon = {
                if (passwordVisibility) {
                    Text("V", modifier = Modifier.clickable(onClick = { passwordVisibility = false }))
                } else {
                    Text("H", modifier = Modifier.clickable(onClick = { passwordVisibility = true }).padding(8.dp))
                }
            }

        )



        Button(
            onClick = { viewModel.onEvent(LoginEvent.Submit) },
            enabled = state !is LoginUiState.Loading,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            if (state is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 1.5.dp,
                )
            } else {
                Text("Login")
            }
        }

        if (state is LoginUiState.Error) {
            Text(
                text = state.message,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
