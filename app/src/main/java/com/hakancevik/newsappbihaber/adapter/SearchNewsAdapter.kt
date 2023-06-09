package com.hakancevik.newsappbihaber.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.hakancevik.newsappbihaber.databinding.RecyclerRowSearchNewsBinding
import com.hakancevik.newsappbihaber.model.Article
import javax.inject.Inject

class SearchNewsAdapter @Inject constructor(
    private val glide: RequestManager

) : RecyclerView.Adapter<SearchNewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: RecyclerRowSearchNewsBinding) : RecyclerView.ViewHolder(binding.root)


    private val diffUtil = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    var newsList: List<Article>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = RecyclerRowSearchNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {


        val article = newsList[position]

        holder.itemView.apply {
            glide.load(article.urlToImage).into(holder.binding.articleImageView)
            holder.binding.sourceText.text = article.source?.name
            holder.binding.titleText.text = article.title
            holder.binding.publishedAtText.text = article.publishedAt


            setOnClickListener {
                onItemClickListener?.let {
                    it(article)
                }
            }

        }
    }


    private var onItemClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

    fun resetData() {
        newsList = emptyList()
        notifyDataSetChanged()
    }

    fun updateNewsList(updatedNewsList: List<Article>) {
        newsList = updatedNewsList
        notifyDataSetChanged()
    }


}