package com.example.lucene.data.model.request

data class TmdbMovie(
    val id: Int,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val posterPath: String?
)
