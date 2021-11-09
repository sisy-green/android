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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
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
        painter = rememberImagePainter(it.asset, builder = { size(OriginalSize) }),
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
          painter = rememberImagePainter(it.asset, builder = { size(OriginalSize) }),
          contentDescription = it.description,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit,
        )
      }
    }
  }
}