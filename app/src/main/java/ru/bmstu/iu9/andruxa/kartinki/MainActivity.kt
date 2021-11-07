package ru.bmstu.iu9.andruxa.kartinki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = MainViewModel()
        setContent {
            KartinkiTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") { MainList(navController, viewModel) }
                        composable("image/{imageId}") { backStackEntry ->
                            ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class ImagesSearchModel(
    val data: List<ImageModel>,
) {
    class ImageModel(
        val id: String,
        val assets: Map<String, ImageAssetModel>,
        val description: String,
    )

    class ImageAssetModel(
        val url: String,
    )
}

data class Image(
    val id: String,
    val asset: ImagesSearchModel.ImageAssetModel,
    val description: String,
)

interface ApiService {
    @Headers(
        "Accept: application/json",
        "Authorization: Bearer v2/cUd1Z1hFbWk4WGVpUmlkNmVqQUwzdE1ub3BrUDBUSm4vMzE0OTExNjg0L2N1c3RvbWVyLzQvVEhVNC1sdFBacVdEaW43NThkOUtGNmVHSi16dk5ZQmlDYUJkREREQl9NbzVIZVVfb1NRX3hQRWpRNGRiYzlkMFRGdnhtM1UxX3k0b2VrcHVwM2hUUWM5eTNnNUNfWF9YMURfSDNyLTd2QjljbmZQN2dyVkctMlFGMnZ4SDZhRjlMRnJTeXVteDdIWmhRSHZVM3FMenZlNHBVZjV5czZqVXJZay1HSmlsY2cyWExIdUkwSEE2WnB4c2E5clNjYWszQ044ZWprNk5ZVHhqRU42QW5tQW9hdy9ra1VsWGQ1WW1SM0dRMThkMGNEcU9B",
    )
    @GET("images/search")
    suspend fun searchImages(
        @Query("fields") fields: String = "data(id,assets/preview/url,description)",
    ) : ImagesSearchModel

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

class MainViewModel: ViewModel() {
    val images = mutableStateListOf<Image>()

    init {
        viewModelScope.launch {
            val apiService = ApiService.getInstance()
            images.addAll(0, apiService.searchImages().data.map { Image(
                it.id,
                it.assets["preview"]!!,
                it.description,
            )})
        }
    }
}

@Composable
fun MainList(navController: NavController, viewModel: MainViewModel) {
    val images = remember { viewModel.images }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            items = images,
            key = { it.id },
            itemContent = { Image(
                painter = rememberImagePainter(it.asset.url, builder = { size(OriginalSize) }),
                contentDescription = it.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { navController.navigate("image/${it.id}") },
                contentScale = ContentScale.FillWidth,
            )},
        )
    }
}

@Composable
fun ImageViewer(id: String?, viewModel: MainViewModel) {
    id?.let {
        val image = remember { viewModel.images.find { it.id == id }}
        image?.let {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().background(Color(R.color.black)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = rememberImagePainter(it.asset.url, builder = { size(OriginalSize) }),
                    contentDescription = it.description,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}