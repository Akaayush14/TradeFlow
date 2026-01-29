package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.ReviewModel
import com.example.tradeflow.repository.ReviewRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(private val repo: ReviewRepo) : ViewModel() {

    private val _reviews = MutableStateFlow<List<ReviewModel>>(emptyList())
    val reviews: StateFlow<List<ReviewModel>> = _reviews.asStateFlow()

    private val _userReviews = MutableStateFlow<List<ReviewModel>>(emptyList())
    val userReviews: StateFlow<List<ReviewModel>> = _userReviews.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit) {
        repo.addReview(review, callback)
    }

    fun getReviewsByProductId(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getReviewsByProductId(productId) { success, message, data ->
                _loading.value = false
                if (success) {
                    _reviews.value = data ?: emptyList()
                } else {
                    _reviews.value = emptyList()
                }
            }
        }
    }

    fun getReviewsByUserId(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getReviewsByUserId(userId) { success, message, data ->
                _loading.value = false
                if (success) {
                    _userReviews.value = data ?: emptyList()
                } else {
                    _userReviews.value = emptyList()
                }
            }
        }
    }

    fun getOwnerStats(productIds: List<String>, callback: (Float, Int) -> Unit) {
        if (productIds.isEmpty()) {
            callback(0f, 0)
            return
        }

        viewModelScope.launch {
            var totalRating = 0f
            var totalCount = 0
            var processedCount = 0

            productIds.forEach { productId ->
                repo.getReviewsByProductId(productId) { success, _, reviewsList ->
                    if (success && reviewsList != null) {
                        reviewsList.forEach { r ->
                            totalRating += r.rating
                            totalCount++
                        }
                    }
                    processedCount++
                    if (processedCount == productIds.size) {
                        val average = if (totalCount > 0) totalRating / totalCount else 0f
                        callback(average, totalCount)
                    }
                }
            }
        }
    }
}