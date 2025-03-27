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
        configureListeners()

        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.event.observe(viewLifecycleOwner) { event ->
            processEvent(event)
        }
    }

    private fun configureListeners() = with(binding) {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(query: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(query: Editable?) {
                val finalQuery = query?.toString().orEmpty()
                Log.i(TAG, "User typed query: $finalQuery")
                viewModel.startAction(SearchAction.SearchQuery(finalQuery))
            }
        })
    }

    private fun processEvent(event: BaseEvent) {
        when (event) {
            BaseEvent.ShowLoadingDialog -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
                binding.resultsRecyclerView.visibility = View.GONE
            }
            BaseEvent.DismissLoadingDialog -> {
                binding.progressBar.visibility = View.GONE
            }
            is SearchEvent.Success -> {
                Log.i(TAG, "Event: Search Success with ${event.films.size} results")
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE
                binding.resultsRecyclerView.visibility = View.VISIBLE
                val adapter = FilmAdapter(event.films)
                binding.resultsRecyclerView.adapter = adapter
            }
            is SearchEvent.Error -> {
                Log.i(TAG, "Event: Search Error: ${event.message}")
                binding.progressBar.visibility = View.GONE
                binding.resultsRecyclerView.visibility = View.GONE
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
