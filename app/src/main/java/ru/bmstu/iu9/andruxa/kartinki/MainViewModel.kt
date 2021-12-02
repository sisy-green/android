package ru.bmstu.iu9.andruxa.kartinki

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ImageData(
  val id: String,
  val asset: String,
  val description: String,
)

@Serializable
data class Category(
  val id: String,
  val name: String,
)

val ImageSaver = listSaver<ImageData, String>(
  save = { listOf(it.id, it.asset, it.description) },
  restore = { ImageData(it[0], it[1], it[2]) }
)

data class ImagesSearchParams(
  val categoryID: String? = null,
  val sort: String = "popular",
  val query: String? = null,
)

class MainViewModel : ViewModel() {
  val images =  mutableStateListOf<ImageData>()
  val imagesError =  mutableStateOf(false)
  private var lastImagesSearchParams: ImagesSearchParams? = null

  val categories = mutableStateListOf<Category>()
  val categoriesError =  mutableStateOf(false)

  private val apiService = ApiService.getInstance()

  fun searchImages(searchParams: ImagesSearchParams) : List<ImageData> {
    if (
      searchParams != lastImagesSearchParams ||
      (searchParams == lastImagesSearchParams && imagesError.value)
    ) {
      images.clear()
    }
    if (
      lastImagesSearchParams == null ||
      searchParams != lastImagesSearchParams ||
      (searchParams == lastImagesSearchParams && imagesError.value)
    ) {
      viewModelScope.launch {
        try {
          val resp: ImagesSearchModel = apiService.searchImages(
            category = searchParams.categoryID,
            sort = searchParams.sort,
            query = searchParams.query,
          )
          images.addAll(0, resp.data.map {
            ImageData(
              it.id,
              it.assets["preview"]!!.url,
              it.description,
            )
          }.distinctBy { it.id })
          imagesError.value = false
          lastImagesSearchParams = searchParams
        } catch (e: Exception) {
          imagesError.value = true
        }
      }
    }
    return images
  }

  fun getCategories() : List<Category> {
    if (categories.size == 0) {
      viewModelScope.launch {
        try {
          categories.addAll(0, apiService.getCategories().data)
          categoriesError.value = false
        } catch (e: Exception) {
          categoriesError.value = true
        }
      }
    }
    return categories
  }

  suspend fun getImagesUpdates() : Boolean {
    try {
      return apiService.getImagesUpdates().data.size > 0
    } catch (e: Exception) {
      return false
    }
  }
}


