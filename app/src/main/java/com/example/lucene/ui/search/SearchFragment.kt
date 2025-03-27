package com.example.lucene.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lucene.R
import com.example.lucene.mvi.search.SearchViewModel
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var resultsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)
        resultsTextView = view.findViewById(R.id.resultsTextView)

        viewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)

        viewModel.event.observe(viewLifecycleOwner) { event ->
            processEvent(event)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                viewModel.startAction(SearchAction.SearchQuery(query))
            }
        })
    }

    private fun processEvent(event: BaseEvent) {
        when (event) {
            BaseEvent.ShowLoadingDialog -> {
                progressBar.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
                resultsTextView.visibility = View.GONE
            }
            BaseEvent.DismissLoadingDialog -> {
                progressBar.visibility = View.GONE
            }
            is SearchEvent.Success -> {
                progressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                resultsTextView.visibility = View.VISIBLE
                val resultString = event.films.joinToString(separator = "\n") { film ->
                    "${film.title} - ${film.description}"
                }
                resultsTextView.text = resultString
            }
            is SearchEvent.Error -> {
                progressBar.visibility = View.GONE
                resultsTextView.visibility = View.GONE
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = event.message
            }
        }
    }
}
