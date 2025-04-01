package com.example.lucene.utils

import com.example.lucene.data.model.request.TmdbMovie
import com.example.lucene.utils.Constants.FIELD_ID
import com.example.lucene.utils.Constants.FIELD_OVERVIEW
import com.example.lucene.utils.Constants.FIELD_TITLE
import com.example.lucene.utils.Constants.FIELD_YEAR
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.search.*
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory

/**
 * LuceneMovieIndexer is responsible for indexing TmdbMovie objects
 * and executing search queries on the indexed data.
 *
 * It builds a Lucene index using a list of movies and provides methods to:
 * - Add new movies without losing the existing index.
 * - Search movies by general text query (using TITLE and OVERVIEW fields).
 * - Search movies by an exact year query.
 */
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

    /**
     * Adds new movies to the existing index without losing the previous data.
     * After adding, the index reader and searcher are updated to include the new documents.
     *
     * @param newMovies List of TmdbMovie objects to be added to the index.
     */
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

    /**
     * Executes a search query over the index using TITLE, OVERVIEW and YEAR fields.
     * Boost values are applied to give higher relevance to the title.
     *
     * @param queryStr The search query as a string.
     * @return A list of TmdbMovie objects matching the query.
     */
    fun search(queryStr: String): List<TmdbMovie> {
        if (queryStr.isBlank()) return emptyList()

        val boosts = mapOf(
            FIELD_TITLE to 5.0f,
            FIELD_YEAR to 4.0f,
            FIELD_OVERVIEW to 1.0f
        )
        val fields = arrayOf(FIELD_TITLE, FIELD_YEAR, FIELD_OVERVIEW)
        val parser = MultiFieldQueryParser(fields, analyzer, boosts)
        val query = parser.parse(queryStr)

        val topDocs = indexSearcher.search(query, 10)
        return topDocsToMovies(topDocs)
    }

    /**
     * Executes a search query that matches movies by an exact release year.
     *
     * @param year The release year to search for.
     * @return A list of TmdbMovie objects that have the specified release year.
     */
    fun searchByYear(year: String): List<TmdbMovie> {
        if (year.isBlank()) return emptyList()

        // Build a TermQuery on the YEAR field.
        val termQuery = TermQuery(Term(FIELD_YEAR, year))
        val topDocs = indexSearcher.search(termQuery, 10)

        return topDocsToMovies(topDocs)
    }

    /**
     * Creates a Lucene Document for the given TmdbMovie.
     * It stores the movie's id, title, overview, extracted release year, and the full release date.
     *
     * @param movie The TmdbMovie to index.
     * @return A Document representing the movie.
     */
    private fun createDocument(movie: TmdbMovie): Document {
        val doc = Document()
        doc.add(StringField(FIELD_ID, movie.id.toString(), Field.Store.YES))
        doc.add(TextField(FIELD_TITLE, movie.title, Field.Store.YES))
        doc.add(TextField(FIELD_OVERVIEW, movie.overview, Field.Store.YES))

        if (!movie.releaseDate.isNullOrBlank()) {
            if (movie.releaseDate.length >= 4) {
                val year = movie.releaseDate.take(4)
                doc.add(StringField(FIELD_YEAR, year, Field.Store.YES))
            }
            doc.add(StringField("releaseDateFull", movie.releaseDate, Field.Store.YES))
        }
        return doc
    }

    /**
     * Converts Lucene TopDocs into a list of TmdbMovie objects.
     *
     * @param topDocs The TopDocs object returned from a search.
     * @return A list of TmdbMovie objects reconstructed from the search results.
     */
    private fun topDocsToMovies(topDocs: TopDocs): List<TmdbMovie> {
        val results = mutableListOf<TmdbMovie>()
        for (scoreDoc: ScoreDoc in topDocs.scoreDocs) {
            val doc = indexSearcher.doc(scoreDoc.doc)
            val storedReleaseDate = doc.get("releaseDateFull") ?: ""
            results.add(
                TmdbMovie(
                    id = doc.get(FIELD_ID)?.toIntOrNull() ?: 0,
                    title = doc.get(FIELD_TITLE) ?: "",
                    overview = doc.get(FIELD_OVERVIEW) ?: "",
                    releaseDate = storedReleaseDate,
                    posterPath = null
                )
            )
        }
        return results
    }
}
