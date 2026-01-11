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
}