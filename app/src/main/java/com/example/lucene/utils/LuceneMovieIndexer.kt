package com.example.lucene.utils

import com.example.lucene.data.model.request.TmdbMovie
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
    private val indexSearcher: IndexSearcher

    init {
        val config = IndexWriterConfig(analyzer)
        val writer = IndexWriter(directory, config)
        movies.forEach { movie ->
            val doc = Document().apply {
                add(StringField("id", movie.id.toString(), Field.Store.YES))
                add(TextField("title", movie.title, Field.Store.YES))
                add(TextField("overview", movie.overview, Field.Store.YES))
            }
            writer.addDocument(doc)
        }
        writer.close()
        val reader = DirectoryReader.open(directory)
        indexSearcher = IndexSearcher(reader)
    }

    fun search(queryStr: String): List<TmdbMovie> {
        val results = mutableListOf<TmdbMovie>()
        if (queryStr.isBlank()) return results
        val fields = arrayOf("title", "overview")
        val parser = MultiFieldQueryParser(fields, analyzer)
        val query: Query = parser.parse(queryStr)
        val topDocs: TopDocs = indexSearcher.search(query, 50)
        for (scoreDoc: ScoreDoc in topDocs.scoreDocs) {
            val doc = indexSearcher.doc(scoreDoc.doc)
            val movie = TmdbMovie(
                id = doc.get("id")?.toIntOrNull() ?: 0,
                title = doc.get("title") ?: "",
                overview = doc.get("overview") ?: "",
                releaseDate = "",
                posterPath = null
            )
            results.add(movie)
        }
        return results
    }
}
