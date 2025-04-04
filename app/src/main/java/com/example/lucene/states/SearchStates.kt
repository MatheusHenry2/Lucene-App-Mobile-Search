package com.example.lucene.states

import com.example.lucene.data.model.response.TmdbMovie

sealed class SearchAction {
    data class SearchQuery(val query: String) : SearchAction()
    data class SearchYear(val year: String) : SearchAction()
    data object ToggleBoostAction : SearchAction()
    data class SearchQueryWithGenres(val query: String, val selectedGenres: Set<String>) : SearchAction()
}

sealed class SearchEvent : BaseEvent {
    data class Success(val movies: List<TmdbMovie>) : SearchEvent()
    data class Error(val message: String) : SearchEvent()
    data class MoviesIndexed(val total: Int) : SearchEvent()
}

