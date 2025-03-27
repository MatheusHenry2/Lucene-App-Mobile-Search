package com.example.lucene.mvi.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lucene.data.repository.LuceneFilmRepository
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _event = MutableLiveData<BaseEvent>()
    val event: LiveData<BaseEvent> get() = _event

    private var repository: LuceneFilmRepository? = null

    init {
        initRepository()
    }

    private fun initRepository() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository = LuceneFilmRepository(getApplication())
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Success(emptyList()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Error("Falha ao inicializar: ${e.message}"))
                }
            }
        }
    }

    private fun setEvent(event: BaseEvent) {
        _event.value = event
    }

    fun startAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchQuery -> doSearch(action.query)
        }
    }

    private fun doSearch(query: String) {
        setEvent(BaseEvent.ShowLoadingDialog)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = repository?.search(query).orEmpty()
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Success(results))
                    setEvent(BaseEvent.DismissLoadingDialog)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    setEvent(SearchEvent.Error("Erro na busca: ${e.message}"))
                    setEvent(BaseEvent.DismissLoadingDialog)
                }
            }
        }
    }
}
