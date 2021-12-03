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
  val imagesRefreshing =  mutableStateOf(false)
  val imagesError =  mutableStateOf(false)
  private var lastImagesSearchParams: ImagesSearchParams? = null

  val categories = mutableStateListOf<Category>()
  val categoriesRefreshing =  mutableStateOf(false)
  val categoriesError =  mutableStateOf(false)

  private val apiService = ApiService.getInstance()

  fun searchImages(searchParams: ImagesSearchParams) : List<ImageData> {
    if (
      lastImagesSearchParams == null ||
      searchParams != lastImagesSearchParams ||
      (searchParams == lastImagesSearchParams && imagesError.value)
    ) {
      imagesRequest(searchParams)
    }
    return images
  }

  fun updateImages(searchParams: ImagesSearchParams) {
    imagesRefreshing.value = true
    imagesRequest(searchParams)
  }

  private fun imagesRequest(searchParams: ImagesSearchParams) {
    images.clear()
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
        imagesError.value = lastImagesSearchParams != searchParams && images.size == 0
      } finally {
        imagesRefreshing.value = false
      }
    }
  }

  fun getCategories() : List<Category> {
    if (categories.size == 0) {
      categoriesRequest()
    }
    return categories
  }

  fun updateCategories() {
    categoriesRefreshing.value = true
    categoriesRequest()
  }

  private fun categoriesRequest() {
    viewModelScope.launch {
      try {
        categories.addAll(0, apiService.getCategories().data)
        categoriesError.value = false
      } catch (e: Exception) {
        categoriesError.value = categories.size == 0
      } finally {
        categoriesRefreshing.value = false
      }
    }
  }
}


