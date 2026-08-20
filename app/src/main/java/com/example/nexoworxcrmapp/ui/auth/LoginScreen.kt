// NEW FILE — add at ui/auth/LoginScreen.kt

package com.example.nexoworxcrmapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.ui.theme.AccentGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Danger
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * First-cut login screen (per mentor's scope for this step): validates input
 * client-side, then "lands" the user in the org we're already connected to.
 * No credentials are sent anywhere yet — NetworkModule's existing
 * client_credentials flow keeps handling real API auth underneath. This
 * screen owns the seam: swap [onLoginSuccess]'s trigger for a real OAuth
 * call later without touching this UI.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val orgHost = remember {
        NetworkModule.instanceUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
    }

    fun attemptLogin() {
        val trimmedEmail = email.trim()
        errorText = when {
            trimmedEmail.isEmpty() -> "Enter your Salesforce username or email"
            !trimmedEmail.contains("@") -> "That doesn't look like a valid username"
            password.isEmpty() -> "Enter your password"
            else -> null
        }
        if (errorText != null || isLoading) return

        isLoading = true
        scope.launch {
            delay(650) // placeholder for the real OAuth round-trip, added next
            isLoading = false
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CrmSurface),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Hero panel -----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Forest, MidGreen),
                        ),
                    )
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CrmSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "N",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NexoworxCRM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrmSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sales, on the road or off the grid",
                        fontSize = 13.sp,
                        color = CrmSurface.copy(alpha = 0.85f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Form -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
            ) {
                Text(
                    text = "Welcome back",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                )
                Text(
                    text = "Sign in to pick up where you left off",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 24.dp),
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorText = null },
                    label = { Text("Username or email") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null, tint = MutedGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        cursorColor = Forest,
                        focusedLabelColor = Forest,
                    ),
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorText = null },
                    label = { Text("Password") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MutedGreen) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = MutedGreen,
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        cursorColor = Forest,
                        focusedLabelColor = Forest,
                    ),
                )

                AnimatedVisibility(visible = errorText != null) {
                    Text(
                        text = errorText.orEmpty(),
                        color = Danger,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = Forest),
                    )
                    Text(
                        text = "Keep me signed in",
                        fontSize = 13.sp,
                        color = Charcoal,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { attemptLogin() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Forest,
                        contentColor = CrmSurface,
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = CrmSurface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- Connected-org footer ------------------------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = if (orgHost.isNotBlank()) "Connected to $orgHost" else "Connected to org",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
