package com.akrubastudios.playquizgames.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.material3.Slider
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.performance.DevicePerformanceDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAdvancedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(64.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.settings_title))
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
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
                .verticalScroll(rememberScrollState())
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
                onCheckedChange = { isEnabled -> viewModel.onMusicToggle(isEnabled) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = uiState.musicVolume,
                    onValueChange = { newVolume -> viewModel.onVolumeChange(newVolume) },
                    enabled = uiState.isMusicEnabled,
                    modifier = Modifier.weight(1f) // El Slider ocupa el espacio restante
                )
                // Texto que muestra el porcentaje
                Text(
                    text = "${(uiState.musicVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(40.dp) // Ancho fijo para evitar que el layout "salte"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            SettingRow(
                title = stringResource(R.string.settings_sfx),
                checked = uiState.areSfxEnabled,
                onCheckedChange = { isEnabled -> viewModel.onSfxToggle(isEnabled) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = uiState.sfxVolume,
                    onValueChange = { newVolume -> viewModel.onSfxVolumeChange(newVolume) },
                    enabled = uiState.areSfxEnabled,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(uiState.sfxVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(40.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- INICIO DE LA NUEVA SECCIÓN DE GRÁFICOS ---
            SectionTitle(stringResource(R.string.settings_graphics_section))

            // Fila para seleccionar la calidad
            ClickableRow(
                title = stringResource(R.string.settings_graphics_quality),
                value = tierToDisplayName(tier = uiState.currentQualityTier), // Muestra el tier actual
                onClick = { showQualityDialog = true }
            )

            // Fila para el toggle de ajuste automático
            SettingRow(
                title = stringResource(R.string.settings_auto_adjust),
                checked = uiState.isAutoAdjustEnabled,
                onCheckedChange = { isEnabled -> viewModel.onAutoAdjustToggled(isEnabled) }
            )

            SettingRow(
                title = stringResource(R.string.settings_ocean_animation), // <-- NECESITAREMOS ESTE NUEVO STRING
                checked = uiState.isOceanAnimationEnabled,
                onCheckedChange = { isEnabled -> viewModel.onOceanAnimationToggle(isEnabled) }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ClickableRow(
                title = stringResource(R.string.settings_advanced_options),
                onClick = { showAdvancedDialog = true }
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
    if (showQualityDialog) {
        QualitySelectionDialog(
            onDismiss = { showQualityDialog = false },
            onTierSelected = { tier ->
                viewModel.onQualityTierSelected(tier)
                showQualityDialog = false
            },
            onAutoSelected = {
                viewModel.onAutomaticQualitySelected()
                showQualityDialog = false
            }
        )
    }
    if (showAdvancedDialog) {
        val toastMessage = stringResource(id = R.string.settings_force_redetection_toast)
        AdvancedOptionsDialog(
            onDismiss = { showAdvancedDialog = false },
            onForceRedetection = {
                viewModel.onForceRedetection()
                // Mostramos un Toast como feedback inmediato para el usuario
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                showAdvancedDialog = false // Cerramos el diálogo
            },
            debugInfo = uiState.debugInfo
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

@Composable
private fun QualitySelectionDialog(
    onDismiss: () -> Unit,
    onTierSelected: (DevicePerformanceDetector.DeviceTier) -> Unit,
    onAutoSelected: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(R.string.settings_quality_dialog_title)) },
        text = {
            Column {
                // Opción para Automático
                Text(
                    text = stringResource(R.string.settings_quality_tier_auto),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAutoSelected() }
                        .padding(vertical = 12.dp)
                )
                Divider()
                // Opción para Muy Baja
                Text(
                    text = stringResource(R.string.settings_quality_tier_very_low),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTierSelected(DevicePerformanceDetector.DeviceTier.VERY_LOW) }
                        .padding(vertical = 12.dp)
                )
                // Opción para Baja
                Text(
                    text = stringResource(R.string.settings_quality_tier_low),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTierSelected(DevicePerformanceDetector.DeviceTier.LOW) }
                        .padding(vertical = 12.dp)
                )
                // Opción para Media
                Text(
                    text = stringResource(R.string.settings_quality_tier_medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTierSelected(DevicePerformanceDetector.DeviceTier.MEDIUM) }
                        .padding(vertical = 12.dp)
                )
                // Opción para Alta
                Text(
                    text = stringResource(R.string.settings_quality_tier_high),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTierSelected(DevicePerformanceDetector.DeviceTier.HIGH) }
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

@Composable
private fun AdvancedOptionsDialog(
    onDismiss: () -> Unit,
    onForceRedetection: () -> Unit,
    debugInfo: String
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(R.string.settings_advanced_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Botón para forzar re-detección
                Button(onClick = onForceRedetection, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.settings_force_redetection),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Información de depuración
                Text(
                    text = stringResource(R.string.settings_debug_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = debugInfo,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace // Fuente monoespaciada para el debug
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                DialogButtonText(text = stringResource(R.string.dialog_button_ok))
            }
        }
    )
}

@Composable
private fun tierToDisplayName(tier: DevicePerformanceDetector.DeviceTier?): String {
    return when (tier) {
        DevicePerformanceDetector.DeviceTier.VERY_LOW -> stringResource(R.string.settings_quality_tier_very_low_A)
        DevicePerformanceDetector.DeviceTier.LOW -> stringResource(R.string.settings_quality_tier_low_B)
        DevicePerformanceDetector.DeviceTier.MEDIUM -> stringResource(R.string.settings_quality_tier_medium_C)
        DevicePerformanceDetector.DeviceTier.HIGH -> stringResource(R.string.settings_quality_tier_high_D)
        null -> stringResource(R.string.settings_quality_tier_auto) // null significa Automático
    }
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
            enabled = true
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

@Composable
private fun ClickableRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}