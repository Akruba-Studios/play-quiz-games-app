package com.akrubastudios.playquizgames.ui.screens.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onSignInComplete: (isNewUser: Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efecto para navegar cuando el inicio de sesión es exitoso
    LaunchedEffect(key1 = uiState.signInResult) {
        uiState.signInResult?.let { result ->
            onSignInComplete(result.isNewUser)
        }
    }

    // Launcher para el flujo de inicio de sesión de Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken
            viewModel.onSignInResult(idToken)
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Error de inicio de sesión de Google. Código: ${e.statusCode} Mensaje: ${e.message}")
            viewModel.onSignInResult(null)
        }
    }

    // Opciones de inicio de sesión
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Centramos verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centramos horizontalmente
    ) {
        // 1. AÑADIMOS NUESTRO LOGO
        Image(
            painter = painterResource(id = R.drawable.logo_splash), // Usa el mismo recurso
            contentDescription = "Logo de Play Quiz Games",
            modifier = Modifier.fillMaxWidth(0.7f) // Ocupa el 70% del ancho
        )

        Spacer(modifier = Modifier.height(64.dp)) // Espacio entre logo y botón

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) }) {
                Text(text = stringResource(R.string.login_button))
            }
        }
    }

    // Muestra errores en un Toast
    uiState.error?.let { error ->
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }
}