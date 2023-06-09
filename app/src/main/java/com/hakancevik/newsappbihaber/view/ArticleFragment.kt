package com.hakancevik.newsappbihaber.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.hakancevik.newsappbihaber.R
import com.hakancevik.newsappbihaber.databinding.FragmentArticleBinding
import com.hakancevik.newsappbihaber.util.customToast

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hakancevik.newsappbihaber.util.gone
import com.hakancevik.newsappbihaber.viewmodel.SavedNewsViewModel


class ArticleFragment : Fragment() {


    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SavedNewsViewModel

    private val args: ArticleFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SavedNewsViewModel::class.java]

        val toolbar = (requireActivity() as AppCompatActivity).findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.mToolbar)
        toolbar?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(it)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

            args.article.title?.let { title ->
                it.title = title
            }

            val backButton = it.navigationIcon
            backButton?.setTint(Color.WHITE)
            backButton?.mutate()?.setBounds(0, 0, 50, 50)
            it.navigationIcon = backButton

            it.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }


        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.gone()


        val article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }


        binding.fab.setOnClickListener {
            viewModel.saveArticle(article)
            activity?.customToast("Successfully saved.")
        }

//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                findNavController().popBackStack()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(callback)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}