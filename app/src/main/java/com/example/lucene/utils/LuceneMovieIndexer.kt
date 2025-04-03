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
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import java.text.Normalizer

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

//    fun search(queryStr: String): List<TmdbMovie> {
//        val normalizedQuery = normalizingQuery(queryStr) // This will remove accents in the word.
//        Log.d(TAG, "query normalized: $normalizedQuery")
//        if (normalizedQuery.isBlank()) return emptyList()
//        //verificar o boolean query para categoria de nomes de atores
//        //pensando em buscar fuzzy querie.
//        val boosts = mapOf(
//            //the 2025
//            FIELD_TITLE to 5.0f, // the king kong 2025
//            FIELD_YEAR to 4.0f,
//        )
//        val fields = arrayOf(FIELD_TITLE, FIELD_YEAR)
//        val parser = MultiFieldQueryParser(fields, analyzer, boosts)
//        val query = parser.parse(normalizedQuery)
//        Log.d(TAG, "query parse: $query")
//
//        val topDocs = indexSearcher.search(query, 30)
//        return topDocsToMovies(topDocs)
//        //pensar em reomendaçoes de filme quando nao exisitr busca
//        //com o woorker para recomendar.
//
//        //estudar facets para nosso projeto, estudar para agrupar pela categoria
//
//        // pensar em trocar o multi field para usar campos diferentes com outros analisadores diferentes
//    }


    fun search(queryStr: String): List<TmdbMovie> {
        val normalizedQuery = normalizingQuery(queryStr) // Normalizar a consulta (remover acentos, etc.)
        Log.d(TAG, "query normalized: $normalizedQuery")
        if (normalizedQuery.isBlank()) return emptyList()

        // Definir boosts para título, ano, etc.
        val boosts = mapOf(
            FIELD_TITLE to 8.0f,   // Títulos têm maior relevância
            FIELD_YEAR to 4.0f,    // O ano também é importante
            FIELD_ACTORS to 2.0f,  // Boost para atores
            FIELD_GENRES to 2.0f   // Boost para gêneros
        )

        // Usar um BooleanQuery para atores e outros campos
        val booleanQuery = BooleanQuery.Builder()

        // 1. Campo Título - Fuzzy Query para permitir erros de digitação
        val titleQuery = QueryParser(FIELD_TITLE, analyzer).parse("$normalizedQuery~")  // Fuzzy para o título
        val boostedTitleQuery = BoostQuery(titleQuery, boosts[FIELD_TITLE] ?: 1.0f)  // Aplicando boost no título
        booleanQuery.add(boostedTitleQuery, BooleanClause.Occur.SHOULD)

        // 2. Campo Ano - Busca exata no ano
        val yearQuery = QueryParser(FIELD_YEAR, analyzer).parse(normalizedQuery)
        val boostedYearQuery = BoostQuery(yearQuery, boosts[FIELD_YEAR] ?: 1.0f)  // Aplicando boost no ano
        booleanQuery.add(boostedYearQuery, BooleanClause.Occur.SHOULD)

        // 3. Campo Atores - Fuzzy Query para permitir erros de digitação nos nomes dos atores
        val actorQuery = QueryParser(FIELD_ACTORS, analyzer).parse("$normalizedQuery~")  // Fuzzy para atores
        val boostedActorQuery = BoostQuery(actorQuery, boosts[FIELD_ACTORS] ?: 1.0f)  // Aplicando boost nos atores
        booleanQuery.add(boostedActorQuery, BooleanClause.Occur.SHOULD)

        // 4. Campo Gêneros - Busca simples por gênero
        val genreQuery = QueryParser(FIELD_GENRES, analyzer).parse(normalizedQuery)
        val boostedGenreQuery = BoostQuery(genreQuery, boosts[FIELD_GENRES] ?: 1.0f)  // Aplicando boost nos gêneros
        booleanQuery.add(boostedGenreQuery, BooleanClause.Occur.SHOULD)

        val topDocs = indexSearcher.search(booleanQuery.build(), 30)
        return topDocsToMovies(topDocs)
    }

    private fun normalizingQuery(queryString: String?): String {
        val normalized = Normalizer.normalize(queryString, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

//    fun searchWithoutBoost(queryStr: String): List<TmdbMovie> {
//        if (queryStr.isBlank()) return emptyList()
//
//        // Não define os boosts aqui
//        val fields = arrayOf(FIELD_TITLE, FIELD_YEAR, FIELD_OVERVIEW)
//        val parser = MultiFieldQueryParser(fields, analyzer)
//        val query = parser.parse(queryStr)
//
//        // Executa a busca sem boosts
//        val topDocs = indexSearcher.search(query, 30)
//        return topDocsToMovies(topDocs)
//    }

    fun searchWithoutBoost(queryStr: String): List<TmdbMovie> {
        val normalizedQuery = normalizingQuery(queryStr)
        Log.d(TAG, "query normalized: $normalizedQuery")
        if (normalizedQuery.isBlank()) return emptyList()

        val booleanQuery = BooleanQuery.Builder()

        val titleQuery = QueryParser(FIELD_TITLE, analyzer).parse("$normalizedQuery~")  // Fuzzy para o título
        booleanQuery.add(titleQuery, BooleanClause.Occur.SHOULD)

        val yearQuery = QueryParser(FIELD_YEAR, analyzer).parse(normalizedQuery)
        booleanQuery.add(yearQuery, BooleanClause.Occur.SHOULD)

        val actorQuery = QueryParser(FIELD_ACTORS, analyzer).parse("$normalizedQuery~")  // Fuzzy para atores
        booleanQuery.add(actorQuery, BooleanClause.Occur.SHOULD)

        val genreQuery = QueryParser(FIELD_GENRES, analyzer).parse(normalizedQuery)
        booleanQuery.add(genreQuery, BooleanClause.Occur.SHOULD)

        val topDocs = indexSearcher.search(booleanQuery.build(), 30)
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
}

