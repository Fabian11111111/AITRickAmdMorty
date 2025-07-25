package com.muhammedesadcomert.rickandmorty.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.muhammedesadcomert.rickandmorty.R
import com.muhammedesadcomert.rickandmorty.domain.model.Character
import com.muhammedesadcomert.rickandmorty.domain.model.Location
import com.muhammedesadcomert.rickandmorty.ui.theme.Yellow
import com.muhammedesadcomert.rickandmorty.util.CharacterGender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navigateToCharacterDetail: (String) -> Unit) {
    val viewModel: HomeViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }

    val characterUiState by viewModel.characters.collectAsStateWithLifecycle()
    val locationLazyPagingItems = viewModel.locations.collectAsLazyPagingItems()

    val isRefreshing = characterUiState.isLoading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    var searchText by rememberSaveable { mutableStateOf("") }

    val filteredCharacters = remember(characterUiState.data, searchText) {
        if (searchText.isBlank()) characterUiState.data
        else characterUiState.data?.filter {
            it.name.contains(searchText, ignoreCase = true)
        } ?: emptyList()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(16.dp),
                hostState = snackBarHostState
            ) { data ->
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.getCharacters() }) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                ) {
                    Text(text = data.visuals.message)
                }
            }
        }
    ) { paddingValues ->

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                viewModel.getCharacters()
                locationLazyPagingItems.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text(stringResource(id = R.string.search_characters)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotBlank()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(id = R.string.clear_search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationRow(
                    modifier = Modifier.fillMaxWidth(),
                    locationLazyPagingItems = locationLazyPagingItems,
                    onButtonClick = { residentUrls ->
                        viewModel.getMultipleCharacters(residentUrls)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CharacterListContent(
                    characters = filteredCharacters,
                    isLoading = characterUiState.isLoading,
                    errorMessage = characterUiState.errorMessage,
                    onRetry = { viewModel.getCharacters() },
                    onCardClick = navigateToCharacterDetail,
                    modifier = Modifier.weight(1f),
                    favoriteIds = viewModel.favoriteIds.collectAsState().value,
                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) }
                )
            }
        }
    }
}

@Composable
fun LocationRow(
    modifier: Modifier,
    locationLazyPagingItems: LazyPagingItems<Location>,
    onButtonClick: (List<String>) -> Unit
) {
    var selectedPosition by rememberSaveable { mutableIntStateOf(-1) }

    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = locationLazyPagingItems.itemSnapshotList.items,
            key = { _, location -> location.id },
            contentType = { _, location -> location }
        ) { index, location ->
            LocationButton(
                text = location.name,
                isSelected = selectedPosition == index
            ) {
                selectedPosition = index
                onButtonClick(location.residents)
            }
        }

        if (locationLazyPagingItems.loadState.append is LoadState.Loading) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun LocationButton(text: String, isSelected: Boolean, onButtonClick: () -> Unit) {
    val containerColor = if (isSelected) Yellow else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer

    Button(
        onClick = onButtonClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//@Composable
//fun CharacterListContent(
//    characters: List<Character>?,
//    isLoading: Boolean,
//    errorMessage: String?,
//    onRetry: () -> Unit,
//    onCardClick: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    favoriteIds: Set<String>,
//    onFavoriteClick: (String) -> Unit
//) {
//    when {
//        isLoading -> {
//            Box(modifier = modifier.fillMaxSize()) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }
//        }
//
//        errorMessage != null -> {
//            Box(modifier = modifier.fillMaxSize()) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = errorMessage,
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Button(onClick = onRetry) {
//                        Text(text = stringResource(R.string.retry))
//                    }
//                }
//            }
//        }
//
//        characters.isNullOrEmpty() -> {
//            Box(modifier = modifier.fillMaxSize()) {
//                Text(
//                    text = stringResource(R.string.empty_state_message),
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//        }
//
//        else -> {
//            CharacterColumn(
//                modifier = modifier,
//                characters = characters,
//                favoriteIds = favoriteIds,
//                onCardClick = onCardClick,
//                onFavoriteClick = onFavoriteClick
//            )
//        }
//    }
//}

@Composable
fun CharacterColumn(
    modifier: Modifier,
    characters: List<Character>,
    favoriteIds: Set<String>,
    onCardClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier
            .padding(vertical = 8.dp)
            .navigationBarsPadding(),
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(characters, key = { it.id }) { character ->
            if (character.name.isNotEmpty() || character.image.isNotEmpty()) {
                CharacterCard(
                    name = character.name,
                    imageUrl = character.image,
                    gender = character.gender.uppercase(),
                    isFavorite = favoriteIds.contains(character.id),
                    onClick = { onCardClick(character.id) },
                    onFavoriteClick = { onFavoriteClick(character.id) }
                )
            }
        }
    }
}

@Composable
fun CharacterCard(
    name: String,
    imageUrl: String,
    gender: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val borderColor = when (gender) {
        CharacterGender.MALE.name -> Color(0xFF9CE5FF)
        CharacterGender.FEMALE.name -> Color(0xFFFFB6C1)
        CharacterGender.GENDERLESS.name -> Color(0xFFFFFF88)
        CharacterGender.UNKNOWN.name -> Color.LightGray
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.character_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )

            Text(
                text = name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily(Font(R.font.rubik_regular))
            )

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.unmark_favorite),
                        tint = Yellow
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.mark_favorite),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
