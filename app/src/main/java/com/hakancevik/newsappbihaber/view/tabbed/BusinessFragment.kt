package com.hakancevik.newsappbihaber.view.tabbed

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hakancevik.newsappbihaber.adapter.SearchNewsAdapter
import com.hakancevik.newsappbihaber.databinding.FragmentBusinessBinding

import com.hakancevik.newsappbihaber.util.Constants.QUERY_PAGE_SIZE

import com.hakancevik.newsappbihaber.util.Resource
import com.hakancevik.newsappbihaber.util.gone
import com.hakancevik.newsappbihaber.util.hide
import com.hakancevik.newsappbihaber.util.show
import com.hakancevik.newsappbihaber.view.CategoriesFragmentDirections
import com.hakancevik.newsappbihaber.viewmodel.tabbed.BusinessViewModel

import javax.inject.Inject


class BusinessFragment @Inject constructor(
    private val searchNewsAdapter: SearchNewsAdapter
) : Fragment() {


    private var _binding: FragmentBusinessBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BusinessViewModel

    private val TAG = "BusinessFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BusinessViewModel::class.java]
        setupRecyclerView()

        viewModel.getBusinessNews("us")


        searchNewsAdapter.setOnItemClickListener {
            val action = CategoriesFragmentDirections.actionCategoriesFragmentToArticleFragment(it)
            findNavController().navigate(action)
        }


        subscribeToObservers()


        binding.connectionTryAgainButton.setOnClickListener {
            viewModel.getBusinessNews("us")
        }


    }


    private fun subscribeToObservers() {

        viewModel.businessNews.observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Success -> {

                    hideProgressBar()
                    viewModel.businessConnectionInfo.value = false

                    response.data?.let { newsResponse ->

                        searchNewsAdapter.newsList = newsResponse.articles?.toList() ?: arrayListOf()

                        if (newsResponse.totalResults != null) {
                            val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.businessNewsPage == totalPages
                            if (isLastPage) {
                                binding.recyclerViewBusiness.setPadding(0, 0, 0, 0)
                            }
                        }

                    }
                }

                is Resource.Error -> {

                    hideProgressBar()
                    viewModel.businessConnectionInfo.value = true
                    response.message?.let { message ->
                        Log.d(TAG, "error: $message")
                        binding.internetConnectionInfoLayout.show()
                    }

                }

                is Resource.Loading -> {
                    showProgressBar()
                    viewModel.businessConnectionInfo.value = false
                }
            }


        }



        viewModel.businessConnectionInfo.observe(viewLifecycleOwner) {
            if (it) {
                binding.internetConnectionInfoLayout.show()
                binding.recyclerViewBusiness.gone()
            } else {
                binding.recyclerViewBusiness.show()
                binding.internetConnectionInfoLayout.gone()
            }
        }


    }


    private fun hideProgressBar() {
        binding.paginationProgressBar.hide()
        binding.recyclerViewBusiness.show()
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.show()
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getBusinessNews("us")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }


    private fun setupRecyclerView() {
        binding.recyclerViewBusiness.apply {
            adapter = searchNewsAdapter
            layoutManager = GridLayoutManager(activity, 2)
            addOnScrollListener(this@BusinessFragment.scrollListener)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    companion object {
        fun newInstance(searchNewsAdapter: SearchNewsAdapter): BusinessFragment {
            return BusinessFragment(searchNewsAdapter)
        }
    }

}