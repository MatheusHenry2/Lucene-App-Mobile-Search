package com.example.lucene.utils

import android.util.Log
import com.example.lucene.data.model.response.TmdbMovie
import com.example.lucene.utils.Constants.FIELD_ACTORS
import com.example.lucene.utils.Constants.FIELD_GENRES
import com.example.lucene.utils.Constants.FIELD_ID
import com.example.lucene.utils.Constants.FIELD_OVERVIEW
import com.example.lucene.utils.Constants.FIELD_RELEASE_DATE_FULL
import com.example.lucene.utils.Constants.FIELD_TITLE
import com.example.lucene.utils.Constants.FIELD_YEAR
import com.example.lucene.utils.Constants.TAG
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BoostQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.PrefixQuery
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import java.text.Normalizer

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

    fun search(queryStr: String): List<TmdbMovie> {
        val normalizedQuery = normalizingQuery(queryStr) // Normalizar a consulta (remover acentos, etc.)
        Log.d(TAG, "query normalized: $normalizedQuery")
        if (normalizedQuery.isBlank()) return emptyList()

        val boosts = mapOf(
            FIELD_TITLE to 8.0f,
            FIELD_YEAR to 4.0f,
            FIELD_ACTORS to 3.0f,
            FIELD_GENRES to 2.0f
        )

        val booleanQuery = BooleanQuery.Builder()

        val prefixQuery = PrefixQuery(Term(FIELD_TITLE, normalizedQuery)) //prefix para o title
        val fuzzyTitleQuery = QueryParser(FIELD_TITLE, analyzer).parse("$normalizedQuery~")  // Fuzzy para o title

        booleanQuery.add(BoostQuery(prefixQuery, boosts[FIELD_TITLE] ?: 1.0f), BooleanClause.Occur.SHOULD)
        booleanQuery.add(BoostQuery(fuzzyTitleQuery, boosts[FIELD_TITLE] ?: 1.0f), BooleanClause.Occur.SHOULD)

        val yearQuery = QueryParser(FIELD_YEAR, analyzer).parse(normalizedQuery)
        val boostedYearQuery = BoostQuery(yearQuery, boosts[FIELD_YEAR] ?: 1.0f)
        booleanQuery.add(boostedYearQuery, BooleanClause.Occur.SHOULD)

        val actorQuery = QueryParser(FIELD_ACTORS, analyzer).parse("$normalizedQuery~")  // Fuzzy para actors
        val boostedActorQuery = BoostQuery(actorQuery, boosts[FIELD_ACTORS] ?: 1.0f)
        booleanQuery.add(boostedActorQuery, BooleanClause.Occur.SHOULD)

        val genreQuery = QueryParser(FIELD_GENRES, analyzer).parse(normalizedQuery)
        val boostedGenreQuery = BoostQuery(genreQuery, boosts[FIELD_GENRES] ?: 1.0f)
        booleanQuery.add(boostedGenreQuery, BooleanClause.Occur.SHOULD)

        val topDocs = indexSearcher.search(booleanQuery.build(), 30)
        return topDocsToMovies(topDocs)
    }

    private fun normalizingQuery(queryString: String?): String {
        val normalized = Normalizer.normalize(queryString, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun searchWithoutBoost(queryStr: String): List<TmdbMovie> {
        val normalizedQuery = normalizingQuery(queryStr)
        Log.d(TAG, "query normalized: $normalizedQuery")
        if (normalizedQuery.isBlank()) return emptyList()

        val booleanQuery = BooleanQuery.Builder()

        val prefixQuery = PrefixQuery(Term(FIELD_TITLE, normalizedQuery)) // PrefixQuery para o title
        val fuzzyTitleQuery = QueryParser(FIELD_TITLE, analyzer).parse("$normalizedQuery~") // Fuzzy para o title

        booleanQuery.add(prefixQuery, BooleanClause.Occur.SHOULD)
        booleanQuery.add(fuzzyTitleQuery, BooleanClause.Occur.SHOULD)

        val yearQuery = QueryParser(FIELD_YEAR, analyzer).parse(normalizedQuery)
        booleanQuery.add(yearQuery, BooleanClause.Occur.SHOULD)

        val actorQuery = QueryParser(FIELD_ACTORS, analyzer).parse("$normalizedQuery~") // Fuzzy para actors
        booleanQuery.add(actorQuery, BooleanClause.Occur.SHOULD)

        val genreQuery = QueryParser(FIELD_GENRES, analyzer).parse(normalizedQuery)
        booleanQuery.add(genreQuery, BooleanClause.Occur.SHOULD)

        val topDocs = indexSearcher.search(booleanQuery.build(), 30)
        return topDocsToMovies(topDocs)
    }

    fun searchByYear(year: String): List<TmdbMovie> {
        if (year.isBlank()) return emptyList()

        // Build a TermQuery on the YEAR field.
        val termQuery = TermQuery(Term(FIELD_YEAR, year))
        val topDocs = indexSearcher.search(termQuery, 30)

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
        doc.add(TextField(FIELD_ACTORS, movie.actors.joinToString(", "), Field.Store.YES))
        doc.add(TextField(FIELD_GENRES, movie.genres.joinToString(", "), Field.Store.YES))

        if (!movie.releaseDate.isNullOrBlank()) {
            if (movie.releaseDate.length >= 4) {
                val year = movie.releaseDate.take(4)
                doc.add(StringField(FIELD_YEAR, year, Field.Store.YES))
            }
            doc.add(StringField(FIELD_RELEASE_DATE_FULL, movie.releaseDate, Field.Store.YES))
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

            val storedReleaseDate = doc.get(FIELD_RELEASE_DATE_FULL) ?: ""
            val storedActors = doc.get(FIELD_ACTORS)?.split(", ") ?: emptyList()
            val storedGenres = doc.get(FIELD_GENRES)?.split(", ") ?: emptyList()

            results.add(
                TmdbMovie(
                    id = doc.get(FIELD_ID)?.toIntOrNull() ?: 0,
                    title = doc.get(FIELD_TITLE) ?: "",
                    overview = doc.get(FIELD_OVERVIEW) ?: "",
                    releaseDate = storedReleaseDate,
                    posterPath = null,
                    actors = storedActors,
                    genres = storedGenres
                )
            )
        }
        return results
    }

    fun searchWithGenres(queryStr: String, selectedGenres: Set<String>): List<TmdbMovie> {
        val normalizedQuery = normalizingQuery(queryStr)
        Log.d(TAG, "query normalized: $normalizedQuery")
        if (normalizedQuery.isBlank()) return emptyList()

        val booleanQuery = BooleanQuery.Builder()

        val titleQuery = QueryParser(FIELD_TITLE, analyzer).parse("$normalizedQuery~")
        booleanQuery.add(titleQuery, BooleanClause.Occur.SHOULD)

        selectedGenres.forEach { genre ->
            val genreQuery = QueryParser(FIELD_GENRES, analyzer).parse(genre)
            booleanQuery.add(genreQuery, BooleanClause.Occur.MUST)
        }

        val topDocs = indexSearcher.search(booleanQuery.build(), 30)
        return topDocsToMovies(topDocs)
    }
}


