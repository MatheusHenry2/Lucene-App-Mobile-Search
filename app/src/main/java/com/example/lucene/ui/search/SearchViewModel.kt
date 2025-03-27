package com.example.lucene.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lucene.data.repository.LuceneFilmRepository
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import com.example.lucene.utils.Constants.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _event = MutableLiveData<BaseEvent>()
    val event: LiveData<BaseEvent> get() = _event

    private var repository: LuceneFilmRepository? = null

    init {
        Log.d(TAG, "Initializing SearchViewModel...")
        initRepository()
    }

    private fun initRepository() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository = LuceneFilmRepository(getApplication())
                withContext(Dispatchers.Main) {
                    Log.w(TAG, "Repository initialized successfully, emitting Success(emptyList)")
                    setEvent(SearchEvent.Success(emptyList()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to initialize repository: ${e.message}")
                    setEvent(SearchEvent.Error("Failed to initialize: ${e.message}"))
                }
            }
        }
    }

    private fun setEvent(event: BaseEvent) {
        _event.value = event
    }

    fun startAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchQuery -> {
                Log.i(TAG, "Received SearchQuery action with query: \"${action.query}\"")
                doSearch(action.query)
            }
        }
    }

    private fun doSearch(query: String) {
        Log.d(TAG, "Starting search for query: \"$query\"")
        setEvent(BaseEvent.ShowLoadingDialog)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = repository?.search(query).orEmpty()
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
}

