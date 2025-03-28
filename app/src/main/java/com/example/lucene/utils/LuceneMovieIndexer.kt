package com.example.lucene.utils

import com.example.lucene.data.model.request.TmdbMovie
import com.example.lucene.utils.Constants.FIELD_ID
import com.example.lucene.utils.Constants.FIELD_OVERVIEW
import com.example.lucene.utils.Constants.FIELD_TITLE
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory

class LuceneMovieIndexer(movies: List<TmdbMovie>) {

    private val analyzer = StandardAnalyzer()
    private val directory: Directory = ByteBuffersDirectory()

    private var indexReader: DirectoryReader
    private var indexSearcher: IndexSearcher

    init {
        val config = IndexWriterConfig(analyzer)
        val writer = IndexWriter(directory, config)

        movies.forEach { movie ->
            val doc = createDocument(movie)
            writer.addDocument(doc)
        }
        writer.close()

        indexReader = DirectoryReader.open(directory)
        indexSearcher = IndexSearcher(indexReader)
    }

    fun addMovies(newMovies: List<TmdbMovie>) {
        if (newMovies.isEmpty()) return

        val config = IndexWriterConfig(analyzer)
        val writer = IndexWriter(directory, config)

        newMovies.forEach { movie ->
            val doc = createDocument(movie)
            writer.addDocument(doc)
        }
        writer.close()

        val newReader = DirectoryReader.openIfChanged(indexReader)
        if (newReader != null) {
            indexReader.close()
            indexReader = newReader
            indexSearcher = IndexSearcher(indexReader)
        }
    }

    fun search(queryStr: String): List<TmdbMovie> {
        if (queryStr.isBlank()) return emptyList()

        val fields = arrayOf(FIELD_TITLE, FIELD_OVERVIEW)
        val parser = MultiFieldQueryParser(fields, analyzer)
        val query: Query = parser.parse(queryStr)

        val topDocs: TopDocs = indexSearcher.search(query, 50)

        val results = mutableListOf<TmdbMovie>()
        for (scoreDoc: ScoreDoc in topDocs.scoreDocs) {
            val doc = indexSearcher.doc(scoreDoc.doc)
            results.add(
                TmdbMovie(
                    id = doc.get(FIELD_ID)?.toIntOrNull() ?: 0,
                    title = doc.get(FIELD_TITLE) ?: "",
                    overview = doc.get(FIELD_OVERVIEW) ?: "",
                    releaseDate = "",
                    posterPath = null
                )
            )
        }
        return results
    }

    private fun createDocument(movie: TmdbMovie): Document {
        return Document().apply {
            add(StringField(FIELD_ID, movie.id.toString(), Field.Store.YES))
            add(TextField(FIELD_TITLE, movie.title, Field.Store.YES))
            add(TextField(FIELD_OVERVIEW, movie.overview, Field.Store.YES))
        }
    }
}
