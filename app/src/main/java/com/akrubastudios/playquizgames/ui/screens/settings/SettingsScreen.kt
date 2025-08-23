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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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

            // Sección de Sonido
            SectionTitle(stringResource(R.string.settings_sound_section))
            SettingRow(
                title = stringResource(R.string.settings_music),
                checked = uiState.isMusicEnabled,
                // 2. Usamos la variable de texto que ya hemos obtenido.
                onCheckedChange = { Toast.makeText(context, featureNotAvailableText, Toast.LENGTH_SHORT).show() }
            )
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