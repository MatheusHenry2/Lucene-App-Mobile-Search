package com.example.lucene.data.repository

import android.content.Context
import android.util.Log
import com.example.lucene.data.model.Film
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory

import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class LuceneFilmRepository(context: Context) {

    private val analyzer: Analyzer = StandardAnalyzer()

    private val directory: Directory = ByteBuffersDirectory()

    private val indexSearcher: IndexSearcher

    init {
        val films = loadMockFilms(context)

        // Configura o IndexWriter para escrever/atualizar o índice.
        val config = IndexWriterConfig(analyzer)
        val writer = IndexWriter(directory, config)

        // Indexa cada filme.
        for (film in films) {
            // Cada filme vira um Document
            val doc = Document().apply {
                // Campos:
                // 1) "id" como StringField (não tokenizado, mas armazenado).
                add(StringField("id", film.id.toString(), Field.Store.YES))

                // 2) "title" como TextField (tokenizado e armazenado).
                add(TextField("title", film.title, Field.Store.YES))

                // 3) "description" como TextField (tokenizado e armazenado).
                add(TextField("description", film.description, Field.Store.YES))
            }
            // Adiciona o doc no índice.
            writer.addDocument(doc)
        }

        writer.close()

        val reader: IndexReader = DirectoryReader.open(directory)
        indexSearcher = IndexSearcher(reader)
    }

    /**
     * Executa uma busca por 'queryStr' nos campos "title" e "description".
     * Retorna lista de [Film] correspondentes.
     */
    fun search(queryStr: String): List<Film> {
        val results = mutableListOf<Film>()

        if (queryStr.isBlank()) {
            return results
        }
        Log.e("query de busca", queryStr)

        // Definimos os campos que queremos pesquisar: "title" e "description".
        val fields = arrayOf("title", "description")

        // MultiFieldQueryParser permite buscar em mais de um campo.
        val parser: QueryParser = MultiFieldQueryParser(fields, analyzer)

        // Faz parse da query string, gerando um objeto Query Lucene.
        val query: Query = parser.parse(queryStr)
        Log.e("query dps do parser", query.toString())

        val topDocs: TopDocs = indexSearcher.search(query, 50)

        for (scoreDoc: ScoreDoc in topDocs.scoreDocs) {
            // Pegamos o Document real pelo docID.
            val doc: Document = indexSearcher.doc(scoreDoc.doc)
            Log.e("doc", doc.toString())

            val film = Film(
                id = doc.get("id")?.toIntOrNull() ?: 0,
                title = doc.get("title") ?: "",
                description = doc.get("description") ?: ""
            )
            results.add(film)
        }

        return results
    }

    /**
     * Lê um arquivo JSON 'movies.json' de /assets, parseia e retorna a lista de Film.
     */
    private fun loadMockFilms(context: Context): List<Film> {
        val inputStream = context.assets.open("movies.json")
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var nRead: Int

        while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        buffer.flush()

        val jsonStr = String(buffer.toByteArray(), StandardCharsets.UTF_8)

        val jsonArr = JSONArray(jsonStr)

        val list = mutableListOf<Film>()

        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            val id = obj.getInt("id")
            val title = obj.getString("title")
            val desc = obj.getString("description")
            list.add(Film(id, title, desc))
        }
        return list
    }
}