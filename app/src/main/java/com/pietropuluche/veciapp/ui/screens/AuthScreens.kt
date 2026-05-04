package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.LoadingBlock
import com.pietropuluche.veciapp.ui.common.ScreenContainer
import com.pietropuluche.veciapp.ui.common.SectionCard
import com.pietropuluche.veciapp.ui.viewmodel.AuthUiState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onReady: (Boolean) -> Unit
) {
    LaunchedEffect(isLoggedIn) {
        delay(900)
        onReady(isLoggedIn)
    }
    ScreenContainer(
        title = "VeciApp",
        subtitle = "Seguridad vecinal en tiempo real"
    ) {
        LoadingBlock("Preparando la aplicacion...")
    }
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    ScreenContainer(
        title = "Ingresar",
        subtitle = "Accede a tus alertas, reportes y familia."
    ) {
        SectionCard {
            InlineMessage(uiState.errorMessage, true)
            InlineMessage(uiState.infoMessage, false)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo") },
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contrasena") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (uiState.isLoading) "Ingresando..." else "Entrar")
            }
            OutlinedButton(
                onClick = onGoRegister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Crear cuenta")
            }
        }
    }
}

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String, String, String) -> Unit
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var documentNumber by rememberSaveable { mutableStateOf("") }

    ScreenContainer(
        title = "Crear cuenta",
        subtitle = "Registra tu perfil para usar VeciApp."
    ) {
        SectionCard {
            InlineMessage(uiState.errorMessage, true)
            InlineMessage(uiState.infoMessage, false)
            OutlinedTextField(firstName, { firstName = it }, Modifier.fillMaxWidth(), label = { Text("Nombres") }, singleLine = true)
            OutlinedTextField(lastName, { lastName = it }, Modifier.fillMaxWidth(), label = { Text("Apellidos") }, singleLine = true)
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Correo") }, singleLine = true)
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Contrasena") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Telefono") }, singleLine = true)
            OutlinedTextField(documentNumber, { documentNumber = it }, Modifier.fillMaxWidth(), label = { Text("Documento") }, singleLine = true)
            Button(
                onClick = { onRegister(firstName, lastName, email, password, phone, documentNumber) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.length >= 8 && phone.isNotBlank()
            ) {
                Text(if (uiState.isLoading) "Creando..." else "Crear cuenta")
            }
        }

        SectionCard {
            Text(
                text = "Tu app se conecta directamente con la API desplegada en Render.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
