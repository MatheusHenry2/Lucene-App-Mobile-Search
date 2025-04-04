package com.example.lucene.ui.search

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucene.data.model.response.TmdbMovie
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import com.example.lucene.utils.Constants.TAG
import com.example.lucene.utils.LuceneMovieIndexer
import com.example.lucene.utils.LuceneMovieIndexerSingleton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _event = MutableLiveData<BaseEvent>()
    val event: LiveData<BaseEvent> get() = _event
    private var luceneIndexer: LuceneMovieIndexer? = null
    var useBoosts = true

    init {
        loadAndIndexPopularMovies()
    }

    private fun loadAndIndexPopularMovies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val movies = loadMoviesFromAssets(getApplication())
                Log.d(
                    TAG,
                    "Movies loaded from file: ${movies.joinToString(separator = ", ") { "(${it.title})" }}"
                )

                if (movies.isNotEmpty()) {
                    luceneIndexer = LuceneMovieIndexer(movies)
                    LuceneMovieIndexerSingleton.indexer = luceneIndexer
                    LuceneMovieIndexerSingleton.totalMoviesCount = movies.size
                    withContext(Dispatchers.Main) {
                        setEvent(SearchEvent.MoviesIndexed(movies.size))
                        setEvent(SearchEvent.Success(emptyList()))
                        Log.i(TAG, "Movies loaded and indexed successfully")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        setEvent(SearchEvent.Error("No movies found in file"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Error("Failed to load and index movies: ${e.message}"))
                }
            }
        }
    }

    private fun loadMoviesFromAssets(context: Context): List<TmdbMovie> {
        return try {
            val inputStream = context.assets.open("movies_data.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            gson.fromJson(json, Array<TmdbMovie>::class.java).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun startAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchQuery -> startSearch(action.query)
            is SearchAction.SearchYear -> searchMoviesByYear(action.year)
            is SearchAction.ToggleBoostAction -> toggleBoosts()
            is SearchAction.SearchQueryWithGenres -> searchMoviesWithGenres(action.query, action.selectedGenres)

        }
    }

    private fun searchMoviesWithGenres(query: String, selectedGenres: Set<String>) {
        launchSearchTask {
            luceneIndexer?.searchWithGenres(query, selectedGenres).orEmpty()
        }
    }

    private fun startSearch(query: String) {
        if (useBoosts) {
            searchMovies(query)
        } else {
            searchMoviesWithoutBoost(query)
        }
    }

    private fun searchMoviesWithoutBoost(query: String) {
        launchSearchTask { luceneIndexer?.searchWithoutBoost(query).orEmpty() }
    }

    private fun searchMovies(query: String) {
        launchSearchTask { luceneIndexer?.search(query).orEmpty() }
    }

    private fun searchMoviesByYear(year: String) {
        launchSearchTask { luceneIndexer?.searchByYear(year).orEmpty() }
    }

    private fun launchSearchTask(searchOperation: suspend () -> List<TmdbMovie>) {
        setEvent(BaseEvent.ShowLoadingDialog)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = searchOperation()
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Success(results))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Error("Search failed: ${e.message}"))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    setEvent(BaseEvent.DismissLoadingDialog)
                }
            }
        }
    }

    private fun setEvent(event: BaseEvent) {
        _event.value = event
    }

    private fun toggleBoosts() {
        useBoosts = !useBoosts
        Log.d(TAG, "toggleBoosts:  ${if (useBoosts) "Activated" else "Disabled"}")

    }
}

