package com.muhammedesadcomert.rickandmorty.ui.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.muhammedesadcomert.rickandmorty.R
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetail(
    characterId: String,
    navigateToBack: () -> Unit
) {
    val viewModel: CharacterDetailViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.getCharacter(characterId)
    }

    val character by viewModel.character.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = character.data?.name ?: "",
                        fontFamily = FontFamily(Font(R.font.rubik_regular)),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(24.dp)
                            .clickable { navigateToBack() },
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.return_to_previous_screen)
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            character.let { uiState ->
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ShowSnackBar(
                                snackbarHostState = snackBarHostState,
                                errorMessage = uiState.errorMessage
                            )
                            Text(
                                text = stringResource(R.string.error_loading_data),
                                fontFamily = FontFamily(Font(R.font.rubik_regular)),
                                fontSize = 18.sp
                            )
                        }
                    }

                    uiState.data != null -> {
                        AsyncImage(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            model = uiState.data.image,
                            contentDescription = stringResource(id = R.string.character_image),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.padding(top = 16.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AttributeRow(title = "Estatus:", text = uiState.data.status)
                                AttributeRow(title = "Especie:", text = uiState.data.species)
                                AttributeRow(title = "Género:", text = uiState.data.gender)
                                AttributeRow(title = "Origen:", text = uiState.data.origin)
                                AttributeRow(title = "Ubicación:", text = uiState.data.location)
                                AttributeRow(title = "Episodios:", text = uiState.data.episodes)
                                AttributeRow(title = "Creado en:", text = uiState.data.created.toShortDateTime())
                            }
                        }

                        Spacer(modifier = Modifier.padding(bottom = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeRow(modifier: Modifier = Modifier, title: String, text: String) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleText(text = title)
        Spacer(modifier = Modifier.width(8.dp))
        RegularText(text = text)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun String.toShortDateTime(): String {
    return try {
        val parsedDate = ZonedDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        parsedDate.format(formatter)
    } catch (e: Exception) {
        this
    }
}

@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily(Font(R.font.rubik_bold))
    )
}

@Composable
fun RegularText(text: String) {
    Text(
        text = text.replaceFirstChar { it.uppercase() },
        fontSize = 16.sp,
        fontFamily = FontFamily(Font(R.font.rubik_regular))
    )
}

@Composable
fun ShowSnackBar(snackbarHostState: SnackbarHostState, errorMessage: String) {
    LaunchedEffect(errorMessage) {
        snackbarHostState.showSnackbar(message = errorMessage)
    }
}