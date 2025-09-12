package com.akrubastudios.playquizgames.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogButtonText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- INICIO DE LA CORRECCIÓN ---
            // 1. Obtenemos el texto del string UNA SOLA VEZ aquí, en el contexto Composable.
            val featureNotAvailableText = stringResource(R.string.feature_not_available)
            val creditsText = stringResource(R.string.credits_toast_text)
            // --- FIN DE LA CORRECCIÓN ---

            // <-- NUEVO: Sección de Idioma -->
            SectionTitle(stringResource(R.string.settings_language_section))
            LanguageSettingRow(
                // Mostramos el nombre completo del idioma actual
                currentLanguage = languageCodeToName(uiState.currentLanguageCode),
                onClick = { showLanguageDialog = true } // Al hacer clic, mostramos el diálogo
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Sección de Sonido
            SectionTitle(stringResource(R.string.settings_sound_section))
            SettingRow(
                title = stringResource(R.string.settings_music),
                checked = uiState.isMusicEnabled,
                // 2. Usamos la variable de texto que ya hemos obtenido.
                onCheckedChange = { Toast.makeText(context, featureNotAvailableText, Toast.LENGTH_SHORT).show() }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            SettingRow(
                title = stringResource(R.string.settings_sfx),
                checked = uiState.areSfxEnabled,
                onCheckedChange = { Toast.makeText(context, featureNotAvailableText, Toast.LENGTH_SHORT).show() }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Sección Legal y Créditos
            SectionTitle(stringResource(R.string.settings_info_section))
            ClickableRow(title = stringResource(R.string.settings_privacy_policy)) {
                Toast.makeText(context, featureNotAvailableText, Toast.LENGTH_SHORT).show()
            }
            ClickableRow(title = stringResource(R.string.settings_credits)) {
                Toast.makeText(context, creditsText, Toast.LENGTH_LONG).show()
            }
        }
    }
    // <-- NUEVO: AlertDialog para seleccionar el idioma -->
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                viewModel.onLanguageSelected(langCode)
                showLanguageDialog = false // Cerramos el diálogo después de seleccionar
            }
        )
    }
}
// <-- NUEVO: Composable para el diálogo de selección de idioma -->
@Composable
private fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    // 1. IMPORTAMOS NUESTRA FUNCIÓN HELPER
    val buttonTextColor = com.akrubastudios.playquizgames.ui.components.getButtonTextColor()

    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column {
                // Opción para Español
                Text(
                    text = stringResource(R.string.language_spanish),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("es") }
                        .padding(vertical = 12.dp)
                )
                // Opción para Inglés
                Text(
                    text = stringResource(R.string.language_english),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                        .padding(vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                DialogButtonText(text = stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

// <-- NUEVO: Composable específico para la fila de ajuste de idioma -->
@Composable
private fun LanguageSettingRow(currentLanguage: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.settings_language), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = currentLanguage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

// <-- NUEVA: Función de utilidad para convertir "es" -> "Español" -->
@Composable
private fun languageCodeToName(code: String): String {
    return when (code) {
        "es" -> stringResource(R.string.language_spanish)
        "en" -> stringResource(R.string.language_english)
        else -> code.uppercase(Locale.getDefault())
    }
}
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = false // Deshabilitado por ahora
        )
    }
}

@Composable
private fun ClickableRow(title: String, onClick: () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}