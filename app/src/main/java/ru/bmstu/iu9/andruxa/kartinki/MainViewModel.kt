package ru.bmstu.iu9.andruxa.kartinki

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class Image(
  val id: String,
  val asset: String,
  val description: String,
)

class MainViewModel: ViewModel() {
  val images = mutableStateListOf<Image>()

  init {
    viewModelScope.launch {
      val apiService = ApiService.getInstance()
      images.addAll(0, apiService.searchImages().data.map { Image(
        it.id,
        it.assets["preview"]!!.url,
        it.description,
      )})
    }
  }
}
