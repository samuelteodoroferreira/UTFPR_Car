package com.example.ftprcar.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ftprcar.ui.main.MainActivity
import com.example.ftprcar.ui.theme.FTPRCarTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            openMain()
            finish()
            return
        }

        setContent {
            val verificationState = remember { mutableStateOf<String?>(null) }
            val loadingState = remember { mutableStateOf(false) }
            FTPRCarTheme {
                LoginScreen(
                    verificationState = verificationState,
                    loadingState = loadingState,
                    onSendCode = { sendCode(it, verificationState, loadingState) },
                    onVerifyCode = { submitCode(it, loadingState) }
                )
            }
        }
    }

    private fun sendCode(
        phone: String,
        verificationState: MutableState<String?>,
        loadingState: MutableState<Boolean>
    ) {
        loadingState.value = true
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential, loadingState)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                loadingState.value = false
                toast("Erro: ${e.message}", Toast.LENGTH_LONG)
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                verificationState.value = id
                loadingState.value = false
                toast("Codigo enviado! Use 101010 para testes.")
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun submitCode(
        code: String,
        loadingState: MutableState<Boolean>
    ) {
        val id = verificationId ?: run {
            toast("Envie o codigo primeiro")
            return
        }
        loadingState.value = true
        signIn(
            credential = PhoneAuthProvider.getCredential(id, code),
            loadingState = loadingState
        )
    }

    private fun signIn(
        credential: PhoneAuthCredential,
        loadingState: MutableState<Boolean>?
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                loadingState?.value = false
                toast("Login realizado com sucesso!")
                openMain()
                finish()
            }
            .addOnFailureListener { e ->
                loadingState?.value = false
                toast("Erro ao verificar: ${e.message}", Toast.LENGTH_LONG)
            }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun toast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(this, message, duration).show()
    }
}

@Composable
fun LoginScreen(
    verificationState: MutableState<String?>,
    loadingState: MutableState<Boolean>,
    onSendCode: (String) -> Unit,
    onVerifyCode: (String) -> Unit
) {
    var phone by remember { mutableStateOf("+5511912345678") }
    var code by remember { mutableStateOf("101010") }
    var showCode by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }
    val loading by loadingState

    LaunchedEffect(verificationState.value) {
        showCode = verificationState.value != null
        if (showCode) inputError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.8f)
            ) {
                Text(
                    text = "FTPR Car",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Login com telefone",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            inputError = null
                        },
                        label = { Text("Número de telefone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !loading,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val trimmedPhone = phone.trim()
                            when {
                                trimmedPhone.isBlank() -> inputError = "Informe o numero de telefone."
                                !trimmedPhone.isValidPhoneNumber() -> inputError = "Use o formato internacional, por exemplo +5511912345678."
                                else -> onSendCode(trimmedPhone)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading && !showCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Enviar código")
                        }
                    }

                    inputError?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    AnimatedVisibility(
                        visible = showCode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = code,
                                onValueChange = {
                                    code = it
                                    inputError = null
                                },
                                label = { Text("Código de verificação") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !loading,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    val trimmedCode = code.trim()
                                    when {
                                        trimmedCode.isBlank() -> inputError = "Informe o codigo de verificacao."
                                        !trimmedCode.isValidVerificationCode() -> inputError = "O codigo deve ter pelo menos 6 digitos."
                                        else -> onVerifyCode(trimmedCode)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !loading,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (loading && showCode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Verificar e entrar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.isValidPhoneNumber(): Boolean {
    return startsWith("+") && length >= 12 && drop(1).all(Char::isDigit)
}

private fun String.isValidVerificationCode(): Boolean {
    return length >= 6 && all(Char::isDigit)
}
