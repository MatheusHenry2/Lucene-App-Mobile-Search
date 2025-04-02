package com.example.lucene.data.model.request

import com.example.lucene.data.model.response.Genre
import com.google.gson.annotations.SerializedName

data class TmdbMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    var actors: List<String> = listOf(),
    var genres: List<String>
)
