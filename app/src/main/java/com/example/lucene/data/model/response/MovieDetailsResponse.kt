package com.example.lucene.data.model.response

data class Genre(
    val id: Int,
    val name: String
)

data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val release_date: String,
    val genres: List<Genre>
)
