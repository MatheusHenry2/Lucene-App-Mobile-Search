package com.example.lucene.states

import com.example.lucene.data.model.Film

sealed class SearchAction {
    data class SearchQuery(val query: String) : SearchAction()
}

sealed class SearchEvent : BaseEvent {
    data class Success(val films: List<Film>) : SearchEvent()
    data class Error(val message: String) : SearchEvent()
}

