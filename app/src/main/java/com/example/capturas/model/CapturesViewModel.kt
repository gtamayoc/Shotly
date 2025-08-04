package com.example.capturas.model

import android.content.ContentResolver
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capturas.capture.Capture
import com.example.capturas.repository.CapturesRepository
import kotlinx.coroutines.launch

class CapturesViewModel : ViewModel() {
    private val _captures = mutableStateOf<List<Capture>>(emptyList())
    val captures: MutableState<List<Capture>> = _captures

    private val _isLoading = mutableStateOf(false)
    val isLoading: MutableState<Boolean> = _isLoading

    private val repository = CapturesRepository()
    private var currentPage = 0
    private val pageSize = 20
    private var hasMore = true
    private val allLoadedCaptures = mutableListOf<Capture>()

    fun loadInitialCaptures(contentResolver: ContentResolver) {
        viewModelScope.launch {
            _isLoading.value = true
            currentPage = 0
            allLoadedCaptures.clear()

            val newCaptures = repository.getCapturesFromGallery(
                contentResolver, pageSize, 0
            )
            allLoadedCaptures.addAll(newCaptures)
            hasMore = newCaptures.size == pageSize

            _captures.value = allLoadedCaptures.toList()
            _isLoading.value = false
        }
    }

    fun loadMoreCaptures(contentResolver: ContentResolver) {
        if (!hasMore || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            currentPage++

            val newCaptures = repository.getCapturesFromGallery(
                contentResolver, pageSize, currentPage * pageSize
            )
            allLoadedCaptures.addAll(newCaptures)
            hasMore = newCaptures.size == pageSize

            _captures.value = allLoadedCaptures.toList()
            _isLoading.value = false
        }
    }

    fun refreshCaptures(contentResolver: ContentResolver) {
        repository.invalidateCache()
        loadInitialCaptures(contentResolver)
    }
}