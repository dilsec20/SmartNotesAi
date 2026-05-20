package com.example.ainote.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ainote.ui.components.CustomTextField
import com.example.ainote.ui.components.GradientButton
import com.example.ainote.ui.theme.GradientEnd
import com.example.ainote.ui.theme.GradientStart

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var startAnim by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(800), label = "alpha"
    )
    LaunchedEffect(Unit) { startAnim = true }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top gradient header
        Box(
            Modifier.fillMaxWidth().height(280.dp)
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
        ) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text("Welcome to", fontSize = 20.sp, color = Color.White.copy(alpha = 0.8f))
                Text("SmartNotes AI", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Form card
        Column(
            Modifier.fillMaxSize().padding(top = 240.dp).alpha(alpha)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        if (isLogin) "Sign In" else "Create Account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Enter your details to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(24.dp))

                    if (!isLogin) {
                        CustomTextField(value = name, onValueChange = { name = it },
                            label = "Full Name", leadingIcon = Icons.Default.Person)
                        Spacer(Modifier.height(12.dp))
                    }
                    CustomTextField(value = email, onValueChange = { email = it },
                        label = "Email", leadingIcon = Icons.Default.Email,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                    Spacer(Modifier.height(12.dp))
                    CustomTextField(value = password, onValueChange = { password = it },
                        label = "Password", leadingIcon = Icons.Default.Lock,
                        isPassword = true, imeAction = ImeAction.Done)
                    Spacer(Modifier.height(24.dp))

                    GradientButton(
                        text = if (isLogin) "Sign In" else "Create Account",
                        onClick = { onLoginSuccess(name.ifBlank { "Student" }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                        Text(
                            if (isLogin) "Don't have an account? " else "Already have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        TextButton(onClick = { isLogin = !isLogin }) {
                            Text(if (isLogin) "Sign Up" else "Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
