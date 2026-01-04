package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.ReviewModel
import com.example.tradeflow.repository.ReviewRepo

class ReviewViewModel(private val repo: ReviewRepo) : ViewModel() {

    private val _reviews = MutableLiveData<List<ReviewModel>?>()
    val reviews: MutableLiveData<List<ReviewModel>?> get() = _reviews

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit) {
        repo.addReview(review, callback)
    }

    fun getReviewsByProductId(productId: String) {
        _loading.value = true
        repo.getReviewsByProductId(productId) { success, message, data ->
            _loading.value = false
            if (success) {
                _reviews.value = data
            } else {
                _reviews.value = emptyList()
            }
        }
    }
}
