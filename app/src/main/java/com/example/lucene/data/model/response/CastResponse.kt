package com.example.lucene.data.model.response

data class CastResponse(
    val cast: List<Actor>
)

data class Actor(
    val name: String
)
