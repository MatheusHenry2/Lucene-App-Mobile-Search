package com.example.lucene.data.model.response

import com.example.lucene.data.model.request.TmdbMovie
import com.google.gson.annotations.SerializedName

data class TMDbMovieResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)
