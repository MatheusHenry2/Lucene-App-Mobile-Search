package com.example.lucene.utils

object LuceneMovieIndexerSingleton {
    var indexer: LuceneMovieIndexer? = null
    var totalMoviesCount: Int = 0
}