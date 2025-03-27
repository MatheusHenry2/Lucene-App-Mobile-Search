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
import com.example.lucene.databinding.FragmentSearchBinding
import com.example.lucene.states.BaseEvent
import com.example.lucene.states.SearchAction
import com.example.lucene.states.SearchEvent
import com.example.lucene.utils.Constants.TAG

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

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                Log.i(TAG, "User typed query: $query")
                viewModel.startAction(SearchAction.SearchQuery(query))
            }
        })

        viewModel.event.observe(viewLifecycleOwner) { event ->
            processEvent(event)
        }
    }

    private fun processEvent(event: BaseEvent) {
        when (event) {
            BaseEvent.ShowLoadingDialog -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
                binding.resultsTextView.visibility = View.GONE
            }
            BaseEvent.DismissLoadingDialog -> {
                binding.progressBar.visibility = View.GONE
            }
            is SearchEvent.Success -> {
                Log.i(TAG, "Event: Search Success with ${event.films.size} results")
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.resultsTextView.visibility = View.VISIBLE
                val resultString = event.films.joinToString(separator = "\n") { film ->
                    "${film.title} - ${film.description}"
                }
                binding.resultsTextView.text = resultString
            }
            is SearchEvent.Error -> {
                Log.i(TAG, "Event: Search Error: ${event.message}")
                binding.progressBar.visibility = View.GONE
                binding.resultsTextView.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = event.message
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
