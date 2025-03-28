package com.example.lucene.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lucene.data.remote.repository.TMDbRepository
import com.example.lucene.utils.LuceneMovieIndexer
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import com.example.lucene.utils.Constants.TAG
import com.example.lucene.utils.LuceneMovieIndexerSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _event = MutableLiveData<BaseEvent>()
    val event: LiveData<BaseEvent> get() = _event

    private val tmdbRepository = TMDbRepository()
    private var luceneIndexer: LuceneMovieIndexer? = null


    init {
        loadAndIndexPopularMovies()
    }

    private fun loadAndIndexPopularMovies() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = tmdbRepository.getPopularMovies()
            val movies = response.results
            Log.d(
                TAG,
                "Movies loaded: ${movies.joinToString(separator = ", ") { "(${it.title})" }}"
            )
            luceneIndexer = LuceneMovieIndexer(movies)
            LuceneMovieIndexerSingleton.indexer = luceneIndexer
            LuceneMovieIndexerSingleton.totalMoviesCount = movies.size
            withContext(Dispatchers.Main) {
                setEvent(SearchEvent.MoviesIndexed(movies.size))
                setEvent(SearchEvent.Success(emptyList()))
                Log.i(TAG, "Movies loaded and indexed successfully")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                setEvent(SearchEvent.Error("Failed to load popular movies: ${e.message}"))
            }
        }
    }

    fun startAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchQuery -> {
                doSearch(action.query)
            }
        }
    }

    private fun doSearch(query: String) {
        Log.i(TAG, "Starting search for query: \"$query\"")
        setEvent(BaseEvent.ShowLoadingDialog)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = luceneIndexer?.search(query).orEmpty()
                withContext(Dispatchers.Main) {
                    Log.i(TAG, "Search successful with ${results.size} results")
                    setEvent(SearchEvent.Success(results))
                    setEvent(BaseEvent.DismissLoadingDialog)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Search failed: ${e.message}")
                    setEvent(SearchEvent.Error("Search failed: ${e.message}"))
                    setEvent(BaseEvent.DismissLoadingDialog)
                }
            }
        }
    }

    private fun setEvent(event: BaseEvent) {
        _event.value = event
    }
}
