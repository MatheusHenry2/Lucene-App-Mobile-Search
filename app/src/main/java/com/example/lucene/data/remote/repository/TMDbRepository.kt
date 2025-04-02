package com.example.lucene.data.remote.repository

import com.example.lucene.data.model.response.CastResponse
import com.example.lucene.data.model.response.MovieDetailsResponse
import com.example.lucene.data.model.response.TMDbMovieResponse
import com.example.lucene.data.remote.service.TMDbApi
import com.example.lucene.utils.Constants.API_KEY

class TMDbRepository {

    suspend fun getPopularMovies(page: Int = 1): TMDbMovieResponse {
        return TMDbApi.retrofitService.getPopularMovies(API_KEY, page)
    }

    suspend fun getMovieCast(movieId: Int): CastResponse {
        return TMDbApi.retrofitService.getMovieCast(movieId, API_KEY)
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetailsResponse {
        return TMDbApi.retrofitService.getMovieDetails(movieId, API_KEY)
    }
}
