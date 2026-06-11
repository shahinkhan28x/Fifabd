package com.example.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.ChannelItem
import com.example.data.model.Country
import com.example.data.repository.StreamRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DiscoveryUiState {
    object Loading : DiscoveryUiState
    data class Success(
        val items: List<ChannelItem>,
        val categories: List<Category>,
        val countries: List<Country>,
        val selectedCategory: Category? = null,
        val selectedCountry: Country? = null,
        val searchQuery: String = ""
    ) : DiscoveryUiState
    data class Error(val message: String) : DiscoveryUiState
}

class DiscoveryViewModel(private val repository: StreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState: StateFlow<DiscoveryUiState> = _uiState

    private var allItems: List<ChannelItem> = emptyList()

    init {
        loadDiscovery()
    }

    fun loadDiscovery() {
        viewModelScope.launch {
            _uiState.value = DiscoveryUiState.Loading
            val items = repository.getDiscoveryItems()
            if (items.isEmpty()) {
                _uiState.value = DiscoveryUiState.Error("Failed to load streams. Check your connection.")
            } else {
                allItems = items
                val categories = items.flatMap { it.categories }.distinctBy { it.id ?: "" }.sortedBy { it.name ?: "" }
                val countries = items.mapNotNull { it.country }.distinctBy { it.code ?: "" }.sortedBy { it.name ?: "" }
                _uiState.value = DiscoveryUiState.Success(
                    items = items,
                    categories = categories,
                    countries = countries
                )
            }
        }
    }

    fun onCategorySelected(category: Category?) {
        val current = _uiState.value as? DiscoveryUiState.Success ?: return
        _uiState.value = current.copy(selectedCategory = category)
        applyFilters()
    }

    fun onCountrySelected(country: Country?) {
        val current = _uiState.value as? DiscoveryUiState.Success ?: return
        _uiState.value = current.copy(selectedCountry = country)
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value as? DiscoveryUiState.Success ?: return
        _uiState.value = current.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value as? DiscoveryUiState.Success ?: return
        val filtered = allItems.filter { item ->
            val matchesCategory = current.selectedCategory == null || item.categories.contains(current.selectedCategory)
            val matchesCountry = current.selectedCountry == null || item.country == current.selectedCountry
            val matchesSearch = current.searchQuery.isEmpty() || 
                    item.channel.name.contains(current.searchQuery, ignoreCase = true) ||
                    item.channel.altNames.any { it.contains(current.searchQuery, ignoreCase = true) }
            
            matchesCategory && matchesCountry && matchesSearch
        }
        _uiState.value = current.copy(items = filtered)
    }
}
