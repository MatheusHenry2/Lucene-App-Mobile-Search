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
import com.example.lucene.utils.Constants.TAG
import com.example.lucene.utils.LuceneMovieIndexerSingleton

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

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
        observeViewModelEvents()
    }

    private fun setupRecyclerView() = with(binding) {
        resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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

