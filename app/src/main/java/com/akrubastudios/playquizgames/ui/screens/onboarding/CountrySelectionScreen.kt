package com.akrubastudios.playquizgames.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CountrySelectionScreen(
    viewModel: CountrySelectionViewModel = hiltViewModel(),
    onCountrySelected: (countryId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Elige tu País de Inicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(uiState.countries) { country ->
                    Button(
                        onClick = { onCountrySelected(country.countryId) },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 4.dp)
                    ) {
                        Text(text = country.name["es"] ?: country.countryId)
                    }
                }
            }
        }
    }
}