package com.example.lucene.data.model.response

import com.example.lucene.data.model.request.TmdbMovie

data class TMDbMovieResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    val totalPages: Int,
    val totalResults: Int
)
