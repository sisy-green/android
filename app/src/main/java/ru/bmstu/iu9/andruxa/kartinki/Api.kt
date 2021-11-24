package ru.bmstu.iu9.andruxa.kartinki

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class ImagesSearchModel(
  val data: List<ImageModel>,
) {
  data class ImageModel(
    val id: String,
    val assets: Map<String, ImageAssetModel>,
    val description: String,
  )

  data class ImageAssetModel(
    val url: String,
  )
}

data class CategoriesGetModel(
  val data: List<Category>
)

interface ApiService {
  @Headers(
    "Accept: application/json",
    "Authorization: Bearer $API_KEY",
  )
  @GET("images/search")
  suspend fun searchImages(
    @Query("fields") fields: String = "data(id,assets/preview/url,description)",
  ) : ImagesSearchModel

  @Headers(
    "Accept: application/json",
    "Authorization: Bearer $API_KEY",
  )
  @GET("images/categories")
  suspend fun getCategories() : CategoriesGetModel

  companion object {
    private var apiService: ApiService? = null
    fun getInstance() : ApiService {
      if (apiService == null) {
        apiService = Retrofit.Builder()
          .baseUrl("https://api.shutterstock.com/v2/")
          .addConverterFactory(GsonConverterFactory.create())
          .build().create(ApiService::class.java)
      }
      return apiService!!
    }
  }
}
