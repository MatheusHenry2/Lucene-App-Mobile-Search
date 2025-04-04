package com.example.lucene.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lucene.R
import com.example.lucene.data.model.response.TmdbMovie

class FilmAdapter(
    private val films: List<TmdbMovie>
) : RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_film, parent, false)
        return FilmViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = films[position]
        holder.bind(film)
    }

    override fun getItemCount(): Int = films.size

    inner class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        private val actorsTextView: TextView = itemView.findViewById(R.id.actorsTextView)
        private val genresTextView: TextView = itemView.findViewById(R.id.movieGenres)

        fun bind(film: TmdbMovie) {
            titleTextView.text = film.title
            descriptionTextView.text = film.overview
            releaseDateTextView.text = itemView.context.getString(R.string.release_date_format, film.releaseDate)
            actorsTextView.text = film.actors.joinToString(", ")
            genresTextView.text = film.genres.joinToString(", ")
        }
    }
}
