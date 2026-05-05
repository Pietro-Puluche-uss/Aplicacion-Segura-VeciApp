package com.pietropuluche.veciapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pietropuluche.veciapp.R
import com.pietropuluche.veciapp.ui.common.InlineMessage
import com.pietropuluche.veciapp.ui.common.LoadingBlock
import com.pietropuluche.veciapp.ui.theme.DeepOcean
import com.pietropuluche.veciapp.ui.theme.SurfaceCard
import com.pietropuluche.veciapp.ui.theme.TextPrimary
import com.pietropuluche.veciapp.ui.theme.TextSecondary
import com.pietropuluche.veciapp.ui.theme.WarmBackground
import com.pietropuluche.veciapp.ui.viewmodel.AuthUiState
import kotlinx.coroutines.delay

private enum class AuthTab {
    LOGIN,
    REGISTER
}

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onReady: (Boolean) -> Unit
) {
    LaunchedEffect(isLoggedIn) {
        delay(900)
        onReady(isLoggedIn)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
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
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var localInfoMessage by rememberSaveable { mutableStateOf("") }

    AuthScreenFrame(
        selectedTab = AuthTab.LOGIN,
        errorMessage = uiState.errorMessage,
        infoMessage = if (uiState.infoMessage.isNotBlank()) uiState.infoMessage else localInfoMessage,
        onSelectLogin = {},
        onSelectRegister = onGoRegister
    ) {
        AuthInputField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo electronico",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        )

        AuthInputField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contrasena (min. 8 caracteres)",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Ver contrasena",
                        tint = TextSecondary
                    )
                }
            }
        )

        Text(
            text = "Olvidaste tu contrasena?",
            style = MaterialTheme.typography.bodySmall,
            color = DeepOcean,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { localInfoMessage = "Recuperacion de contrasena disponible pronto." }
        )

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepOcean,
                contentColor = SurfaceCard
            )
        ) {
            Text(
                text = if (uiState.isLoading) "Ingresando..." else "Ingresar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        AuthSocialSection()
    }
}

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String, String, String) -> Unit,
    onGoLogin: () -> Unit
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var documentNumber by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var localInfoMessage by rememberSaveable { mutableStateOf("") }

    AuthScreenFrame(
        selectedTab = AuthTab.REGISTER,
        errorMessage = uiState.errorMessage,
        infoMessage = if (uiState.infoMessage.isNotBlank()) uiState.infoMessage else localInfoMessage,
        onSelectLogin = onGoLogin,
        onSelectRegister = {}
    ) {
        AuthInputField(
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = "Nombres",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        )

        AuthInputField(
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = "Apellidos",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        )

        AuthInputField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Telefono",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        )

        AuthInputField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo electronico",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        )

        AuthInputField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contrasena",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Ver contrasena",
                        tint = TextSecondary
                    )
                }
            }
        )

        if (documentNumber.isNotBlank()) {
            AuthInputField(
                value = documentNumber,
                onValueChange = { documentNumber = it },
                placeholder = "Documento",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            )
        } else {
            Text(
                text = "Agregar documento es opcional",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { localInfoMessage = "Puedes crear tu cuenta sin documento." }
            )
        }

        Button(
            onClick = { onRegister(firstName, lastName, email, password, phone, documentNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                password.length >= 8 &&
                phone.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepOcean,
                contentColor = SurfaceCard
            )
        ) {
            Text(
                text = if (uiState.isLoading) "Creando..." else "Crear cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        AuthSocialSection()
    }
}

@Composable
private fun AuthScreenFrame(
    selectedTab: AuthTab,
    errorMessage: String,
    infoMessage: String,
    onSelectLogin: () -> Unit,
    onSelectRegister: () -> Unit,
    formContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepOcean)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp, bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13274E))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sdil),
                                contentDescription = "VeciApp",
                                modifier = Modifier
                                    .size(82.dp)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = "VeciApp",
                            style = MaterialTheme.typography.headlineSmall,
                            color = SurfaceCard,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seguridad vecinal a tu alcance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SurfaceCard.copy(alpha = 0.82f)
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AuthTabs(
                            selectedTab = selectedTab,
                            onSelectLogin = onSelectLogin,
                            onSelectRegister = onSelectRegister
                        )

                        InlineMessage(errorMessage, true)
                        InlineMessage(infoMessage, false)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            content = formContent
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun AuthTabs(
    selectedTab: AuthTab,
    onSelectLogin: () -> Unit,
    onSelectRegister: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EAED))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AuthTabButton(
                text = "Iniciar Sesion",
                selected = selectedTab == AuthTab.LOGIN,
                onClick = onSelectLogin,
                modifier = Modifier.weight(1f)
            )
            AuthTabButton(
                text = "Registrarse",
                selected = selectedTab == AuthTab.REGISTER,
                onClick = onSelectRegister,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AuthTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SurfaceCard else Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary
            )
        },
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3F6FB),
            unfocusedContainerColor = Color(0xFFF3F6FB),
            focusedBorderColor = Color(0xFFE3E8F0),
            unfocusedBorderColor = Color(0xFFE3E8F0),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun AuthSocialSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "o continua con",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialButton(
                text = "Google",
                badge = "G",
                modifier = Modifier.weight(1f)
            )
            SocialButton(
                text = "Facebook",
                badge = "f",
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "Al continuar, aceptas nuestros Terminos de Servicio y\nPolitica de Privacidad",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SocialButton(
    text: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E5EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}
