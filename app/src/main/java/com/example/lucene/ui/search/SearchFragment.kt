package com.example.lucene.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lucene.R
import com.example.lucene.data.model.response.TmdbMovie
import com.example.lucene.databinding.FragmentSearchBinding
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import com.example.lucene.utils.Constants.ACTION_GENRE
import com.example.lucene.utils.Constants.ADVENTURE_GENRE
import com.example.lucene.utils.Constants.SCI_FI_GENRE
import com.example.lucene.utils.Constants.TAG
import com.example.lucene.utils.LuceneMovieIndexerSingleton

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private var selectedGenres = mutableSetOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchListener()
        setupSmartQueryButton()
        setupGenreFilterButtons()
        observeViewModelEvents()
    }

    private fun setupRecyclerView() = with(binding) {
        resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupGenreFilterButtons() = with(binding){
        btnSciFi.setOnClickListener { onGenreFilterClicked(it) }
        btnAdventure.setOnClickListener { onGenreFilterClicked(it) }
        btnAction.setOnClickListener { onGenreFilterClicked(it) }
    }

    private fun updateSearchResults() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            viewModel.startAction(SearchAction.SearchQueryWithGenres(query, selectedGenres))
        }
    }

    private fun onGenreFilterClicked(view: View) {
        when (view.id) {
            R.id.btnSciFi -> toggleGenreFilter(SCI_FI_GENRE)
            R.id.btnAdventure -> toggleGenreFilter(ADVENTURE_GENRE)
            R.id.btnAction -> toggleGenreFilter(ACTION_GENRE)
        }
        updateSearchResults()
    }

    private fun toggleGenreFilter(genre: String) {
        if (selectedGenres.contains(genre)) {
            selectedGenres.remove(genre)
        } else {
            selectedGenres.add(genre)
        }
    }

    private fun setupSearchListener() = with(binding) {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty().trim()
                Log.i(TAG, "User typed query: $query")
                if (query.all { it.isDigit() } && query.length == 4) {
                    viewModel.startAction(SearchAction.SearchYear(query))
                } else {
                    viewModel.startAction(SearchAction.SearchQuery(query))
                }
            }
        })
    }

    private fun observeViewModelEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BaseEvent.ShowLoadingDialog -> showLoading()
                BaseEvent.DismissLoadingDialog -> hideLoading()
                is SearchEvent.Success -> showResults(event.movies)
                is SearchEvent.Error -> showError(event.message)
                is SearchEvent.MoviesIndexed -> updateLoadedCount(event.total)
            }
        }
    }

    private fun showLoading() = with(binding) {
        progressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        resultsRecyclerView.visibility = View.GONE
    }

    private fun hideLoading() = with(binding) { progressBar.visibility = View.GONE }

    private fun showResults(films: List<TmdbMovie>) = with(binding) {
        Log.i(TAG, "Search Success with ${films.size} results")
        progressBar.visibility = View.GONE
        errorTextView.visibility = View.GONE
        resultsRecyclerView.visibility = View.VISIBLE
        resultsRecyclerView.adapter = FilmAdapter(films)
    }

    private fun showError(message: String) = with(binding) {
        Log.i(TAG, "Search Error: $message")
        progressBar.visibility = View.GONE
        resultsRecyclerView.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
    }

    private fun setupSmartQueryButton() = with(binding) {
        updateSmartQueryButtonColor()
        smartQueryButton.setOnClickListener {
            viewModel.startAction(SearchAction.ToggleBoostAction)
            updateSmartQueryButtonColor()
            viewModel.startAction(SearchAction.SearchQuery(searchEditText.text.toString().trim()))
        }
    }

    private fun updateSmartQueryButtonColor() = with(binding) {
        if (viewModel.useBoosts) {
            smartQueryButton.setBackgroundColor(resources.getColor(R.color.enabledColor))
        } else {
            smartQueryButton.setBackgroundColor(resources.getColor(R.color.disabledColor))
        }
    }

    private fun updateLoadedCount(total: Int) = binding.loadedMoviesCountTextView.run {
        text = getString(R.string.movies_loaded_and_indexed, total)
        visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        updateLoadedCount(LuceneMovieIndexerSingleton.totalMoviesCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

