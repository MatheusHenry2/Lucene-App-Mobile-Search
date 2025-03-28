package com.example.lucene.states

sealed interface BaseEvent {
    object ShowLoadingDialog : BaseEvent
    object DismissLoadingDialog : BaseEvent
}

sealed interface BaseAction {}

