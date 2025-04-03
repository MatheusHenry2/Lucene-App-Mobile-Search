package com.example.lucene.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.lucene.data.remote.repository.TMDbRepository
import com.example.lucene.utils.AppPreferences
import com.example.lucene.utils.Constants.TAG
import com.example.lucene.utils.LuceneMovieIndexerSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DownloadMoviesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository = TMDbRepository()

    override suspend fun doWork(): Result {
        return try {

            val lastPage = AppPreferences.getLastPage(applicationContext)
            val nextPage = lastPage + 1
            Log.i("DownloadMoviesWorker", "Starting to fetch page = $nextPage")

            // Obter filmes da próxima página
            val newMovies = withContext(Dispatchers.IO) {
                repository.getPopularMovies(page = nextPage).results
            }
            AppPreferences.setLastPage(applicationContext, nextPage)
            Log.d(TAG, "Movies loaded: ${newMovies.joinToString(separator = ", ") { "(${it.title})" }}")
            Log.d(TAG, "Downloaded ${newMovies.size} movies from page $nextPage")

            // Para cada filme, buscar os atores
            newMovies.forEach { movie ->
                // Buscando os atores do filme
                val castResponse = repository.getMovieCast(movie.id)
                movie.actors = castResponse.cast.map { it.name }


                val movieDetails = repository.getMovieDetails(movie.id)
                movie.genres = movieDetails.genres.map { genre -> genre.name }
            }

            // Salvar os filmes com atores em JSON
//            saveMoviesToJson(applicationContext, newMovies)

            // Indexa os filmes com os atores
            val indexer = LuceneMovieIndexerSingleton.indexer
            indexer?.addMovies(newMovies)

            LuceneMovieIndexerSingleton.totalMoviesCount += newMovies.size
            Log.d(TAG, "Total movies indexed so far = ${LuceneMovieIndexerSingleton.totalMoviesCount}")

            // Configura o próximo trabalho para a próxima página
            val nextRequest = OneTimeWorkRequestBuilder<DownloadMoviesWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(nextRequest)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

