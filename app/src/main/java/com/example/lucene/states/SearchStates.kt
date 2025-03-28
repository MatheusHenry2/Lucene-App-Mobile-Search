package com.example.lucene.states

import com.example.lucene.data.model.request.TmdbMovie

sealed class SearchAction {
    data class SearchQuery(val query: String) : SearchAction()
}

sealed class SearchEvent : BaseEvent {
    data class Success(val movies: List<TmdbMovie>) : SearchEvent()
    data class Error(val message: String) : SearchEvent()
    data class MoviesIndexed(val total: Int) : SearchEvent()
}

