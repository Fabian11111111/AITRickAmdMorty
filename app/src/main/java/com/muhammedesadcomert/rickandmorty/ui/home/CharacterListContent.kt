package com.muhammedesadcomert.rickandmorty.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.muhammedesadcomert.rickandmorty.R
import com.muhammedesadcomert.rickandmorty.domain.model.Character

@Composable
fun CharacterListContent(
    characters: List<Character>?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCardClick: (String) -> Unit,
    favoriteIds: Set<String>,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        errorMessage != null -> {
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }
        }

        characters.isNullOrEmpty() -> {
            Box(modifier = modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.empty_state_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        else -> {
            CharacterColumn(
                modifier = modifier,
                characters = characters,
                favoriteIds = favoriteIds,           // PASAMOS LOS FAVORITOS
                onCardClick = onCardClick,
                onFavoriteClick = onFavoriteClick    // PASAMOS EL CALLBACK
            )
        }
    }
}