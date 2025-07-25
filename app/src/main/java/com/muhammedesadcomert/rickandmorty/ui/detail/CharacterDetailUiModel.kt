package com.muhammedesadcomert.rickandmorty.ui.detail

data class CharacterUIModel(
    val id: String,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String,
    val location: String,
    val image: String,
    val episodes: String, // puede ser una lista o un número según tu lógica
    val created: String
)