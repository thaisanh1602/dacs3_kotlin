package com.example.angrismart.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.remote.MapApiService
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FarmlandPolygon(
    val id: Long,
    val points: List<LatLng>,
    val tags: Map<String, String>
)

class MapViewModel(private val apiService: MapApiService) : ViewModel() {
    
    private val _farmlands = MutableStateFlow<List<FarmlandPolygon>>(emptyList())
    val farmlands = _farmlands.asStateFlow()

    private val _selectedFarmland = MutableStateFlow<FarmlandPolygon?>(null)
    val selectedFarmland = _selectedFarmland.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _mapCenter = MutableStateFlow<LatLng?>(null)
    val mapCenter = _mapCenter.asStateFlow()

    // Bounding Box Flow for debouncing map movements
    private val boundingBoxFlow = MutableSharedFlow<LatLngBounds>(extraBufferCapacity = 1)

    // Cache to prevent duplicate parsing
    private val loadedFarmlandIds = mutableSetOf<Long>()

    init {
        observeBoundingBox()
    }

    @OptIn(FlowPreview::class)
    private fun observeBoundingBox() {
        viewModelScope.launch {
            boundingBoxFlow
                .debounce(1000L) // Wait 1 second after map stops moving
                .collect { bbox ->
                    fetchFarmlands(bbox)
                }
        }
    }

    fun onMapBoundsChanged(bbox: LatLngBounds?, zoomLevel: Float) {
        if (bbox != null && zoomLevel >= 14f) {
            boundingBoxFlow.tryEmit(bbox)
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = apiService.searchLocation(query)
                if (results.isNotEmpty()) {
                    val firstResult = results[0]
                    _mapCenter.value = LatLng(firstResult.lat.toDouble(), firstResult.lon.toDouble())
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Location search error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchFarmlands(bbox: LatLngBounds) {
        _isLoading.value = true
        try {
            // Overpass QL Query: get elements with landuse=farmland within bounding box
            val query = """
                [out:json][timeout:25];
                (
                  way["landuse"="farmland"](${bbox.southwest.latitude},${bbox.southwest.longitude},${bbox.northeast.latitude},${bbox.northeast.longitude});
                  relation["landuse"="farmland"](${bbox.southwest.latitude},${bbox.southwest.longitude},${bbox.northeast.latitude},${bbox.northeast.longitude});
                );
                out geom;
            """.trimIndent()

            val response = apiService.getFarmlands(query)
            
            val newPolygons = mutableListOf<FarmlandPolygon>()
            
            response.elements.forEach { element ->
                if (!loadedFarmlandIds.contains(element.id)) {
                    element.geometry?.let { geom ->
                        val latLngs = geom.map { LatLng(it.lat, it.lon) }
                        newPolygons.add(FarmlandPolygon(element.id, latLngs, element.tags ?: emptyMap()))
                        loadedFarmlandIds.add(element.id)
                    }
                }
            }
            
            if (newPolygons.isNotEmpty()) {
                _farmlands.value = _farmlands.value + newPolygons
            }

        } catch (e: Exception) {
                Log.e("MapViewModel", "Overpass fetch error", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun selectFarmland(polygon: FarmlandPolygon) {
        _selectedFarmland.value = polygon
    }
}
