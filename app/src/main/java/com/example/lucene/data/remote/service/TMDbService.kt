package com.example.lucene.data.remote.service

import com.example.lucene.data.model.response.CastResponse
import com.example.lucene.data.model.response.MovieDetailsResponse
import com.example.lucene.data.model.response.TMDbMovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDbService {

    object TMDbConstants {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val PATH_POPULAR_MOVIES = "movie/popular"
        const val QUERY_API_KEY = "api_key"
        const val QUERY_PAGE = "page"
    }

    @GET(TMDbConstants.PATH_POPULAR_MOVIES)
    suspend fun getPopularMovies(
        @Query(TMDbConstants.QUERY_API_KEY) apiKey: String,
        @Query(TMDbConstants.QUERY_PAGE) page: Int = 1
    ): TMDbMovieResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCast(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): CastResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieDetailsResponse

}
