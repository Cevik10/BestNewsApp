package com.hakancevik.newsappbihaber.viewmodel

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hakancevik.newsappbihaber.model.NewsResponse
import com.hakancevik.newsappbihaber.repo.NewsRepository
import com.hakancevik.newsappbihaber.util.Constants.BUSINESS
import com.hakancevik.newsappbihaber.util.Constants.ENTERTAINMENT
import com.hakancevik.newsappbihaber.util.Constants.GENERAL
import com.hakancevik.newsappbihaber.util.Constants.HEALTH
import com.hakancevik.newsappbihaber.util.Constants.SCIENCE
import com.hakancevik.newsappbihaber.util.Constants.SPORTS
import com.hakancevik.newsappbihaber.util.Constants.TECHNOLOGY
import com.hakancevik.newsappbihaber.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {

    val categoryNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var categoryNewsPage = 1
    var categoryNewsResponse: NewsResponse? = null


    fun getCategoryNews(countryCode: String, category: String) = viewModelScope.launch {
        safeCategoryNewsCall(countryCode, category)
    }


    private fun handleCategoryNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                categoryNewsPage++
                if (categoryNewsResponse == null) {
                    categoryNewsResponse = resultResponse
                } else {
                    val oldArticles = categoryNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    if (newArticles != null) {
                        oldArticles?.addAll(newArticles)
                    }
                }
                return Resource.Success(categoryNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    private suspend fun safeCategoryNewsCall(countryCode: String, category: String) {
        categoryNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getCategoryNews(countryCode, category, categoryNewsPage)
                categoryNews.postValue(handleCategoryNewsResponse(response))
            } else {
                categoryNews.postValue(Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> categoryNews.postValue(Resource.Error("Network Failure"))
                else -> categoryNews.postValue(Resource.Error("Conversion Error"))
            }

        }
    }


    private fun hasInternetConnection(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}