import android.content.Context
import android.util.Log
import com.example.lucene.data.model.request.TmdbMovie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type

fun saveMoviesToJson(context: Context, newMovies: List<TmdbMovie>) {
    val gson = Gson()
    val file = File(context.filesDir, "movies_data.json")

    try {
        // Verifica se o arquivo já existe
        val moviesList: MutableList<TmdbMovie> = if (file.exists()) {
            // Se o arquivo existe, lê os dados existentes e converte para a lista de filmes
            val reader = FileReader(file)
            val type: Type = object : TypeToken<List<TmdbMovie>>() {}.type
            val existingMovies: List<TmdbMovie> = gson.fromJson(reader, type)
            reader.close()

            // Cria uma lista mutável com os filmes existentes
            existingMovies.toMutableList()
        } else {
            // Se o arquivo não existe, cria uma nova lista
            mutableListOf()
        }

        // Adiciona os novos filmes à lista existente
        moviesList.addAll(newMovies)

        // Serializa a lista atualizada para JSON
        val json = gson.toJson(moviesList)

        // Salva os dados no arquivo JSON
        val writer = FileWriter(file)
        writer.write(json)
        writer.close()

        Log.d("SaveMovies", "Movies data saved successfully in internal storage.")
        Log.d("SaveMovies", "File saved at: ${file.absolutePath}")

    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("SaveMovies", "Failed to save movies data: ${e.message}")
    }
}
